import { create } from 'zustand';
import { createAudioPlayer, setAudioModeAsync } from 'expo-audio';
import type { AudioPlayer, AudioStatus, AudioMetadata } from 'expo-audio';
import { Accelerometer } from 'expo-sensors';
import { Book, Chapter, HistoryAction } from '../data/types';
import { repo } from '../db/repo';
import { settingsSnapshot } from './settings';

const SAVE_INTERVAL_MS = 3000;
const EXTERNAL_GUARD_MS = 600; // window to attribute a play-state flip to an internal action
const SHAKE_WINDOW_MS = 30000;
const SHAKE_THRESHOLD = 1.8; // g-force delta

let player: AudioPlayer | null = null;
let statusSub: { remove: () => void } | null = null;
let shakeSub: { remove: () => void } | null = null;
let playingSince: number | null = null;
let lastSaveAt = 0;
let lastInternalActionAt = 0;
let transitioning = false; // suppress bookkeeping during chapter swaps
let suppressNextPause = false;
let sleepTimeout: ReturnType<typeof setTimeout> | null = null;
let sleepTick: ReturnType<typeof setInterval> | null = null;

export type SleepMode = number | 'endOfChapter';

interface PlayerState {
  book: Book | null;
  chapterIndex: number;
  playing: boolean;
  positionMs: number;
  durationMs: number;
  rate: number;
  sleepMode: SleepMode | null;
  sleepRemainingMs: number | null;
  awaitingShake: boolean;

  loadBook: (book: Book, chapterIndex?: number, positionMs?: number, autoplay?: boolean) => void;
  play: () => void;
  pause: () => void;
  togglePlay: () => void;
  seekToMs: (ms: number, record?: boolean) => void;
  rewind: () => void;
  fastForward: () => void;
  nextChapter: (auto?: boolean) => void;
  prevChapter: () => void;
  jumpToChapter: (index: number) => void;
  setRate: (rate: number) => void;
  setSleep: (mode: SleepMode | null) => void;
}

export const usePlayer = create<PlayerState>((set, get) => {
  function metadataFor(book: Book, chapter: Chapter | undefined): AudioMetadata {
    return {
      title: book.title,
      artist: book.author ?? undefined,
      albumTitle: chapter?.name ?? undefined,
      artworkUrl: book.coverUri ?? undefined,
    };
  }

  function activateLockScreen() {
    const { book, chapterIndex } = get();
    if (!player || !book) return;
    player.setActiveForLockScreen(true, metadataFor(book, book.chapters[chapterIndex]), {
      showSeekForward: true,
      showSeekBackward: true,
    });
  }

  function consumeListened(keepPlaying: boolean): number {
    if (playingSince == null) return 0;
    const now = Date.now();
    const elapsed = Math.max(0, now - playingSince);
    playingSince = keepPlaying && get().playing ? now : null;
    return elapsed;
  }

  function record(action: HistoryAction, listenedMs: number) {
    const { book, chapterIndex, positionMs } = get();
    const chapter = book?.chapters[chapterIndex];
    if (!book || !chapter) return;
    repo.addHistory({
      bookId: book.id,
      chapterId: chapter.id,
      action,
      positionInChapterMs: positionMs,
      createdAt: Date.now(),
      listenedMs,
    });
  }

  function persist() {
    const { book, chapterIndex, positionMs } = get();
    if (book) repo.savePosition(book.id, chapterIndex, positionMs);
  }

  function applyAutoRewind() {
    if (!player) return;
    const back = settingsSnapshot().autoRewindSeconds * 1000;
    if (back <= 0) return;
    const target = Math.max(0, get().positionMs - back);
    player.seekTo(target / 1000);
    set({ positionMs: target });
  }

  // Single source of truth for play-state changes, whether they originate from the
  // UI, the lock-screen notification, or an audio interruption (call / headphones).
  function handlePlayStateChange(nowPlaying: boolean) {
    const wasPlaying = get().playing;
    if (nowPlaying === wasPlaying) return;
    set({ playing: nowPlaying });

    const external = Date.now() - lastInternalActionAt > EXTERNAL_GUARD_MS;
    if (transitioning) return;

    if (nowPlaying) {
      playingSince = Date.now();
      if (external) record('Played', 0);
    } else {
      const listened = consumeListened(false);
      if (suppressNextPause) {
        suppressNextPause = false;
      } else {
        record('Paused', listened);
      }
      applyAutoRewind();
      persist();
    }
  }

  function onStatus(status: AudioStatus) {
    if (!status.isLoaded) return;
    const positionMs = Math.round((status.currentTime ?? 0) * 1000);
    const durationMs = Math.round((status.duration ?? 0) * 1000) || get().durationMs;
    set({ positionMs, durationMs });
    handlePlayStateChange(status.playing);

    if (status.playing && Date.now() - lastSaveAt > SAVE_INTERVAL_MS) {
      lastSaveAt = Date.now();
      persist();
    }
    if (status.didJustFinish) onChapterFinished();
  }

  function onChapterFinished() {
    if (get().sleepMode === 'endOfChapter') {
      const listened = consumeListened(false);
      record('SleepTimer', listened);
      set({ sleepMode: null });
      suppressNextPause = true;
      player?.pause();
      armShakeWindow();
      return;
    }
    get().nextChapter(true);
  }

  function ensurePlayer(uri: string): AudioPlayer {
    if (!player) {
      player = createAudioPlayer({ uri }, { updateInterval: 500 });
      statusSub = player.addListener('playbackStatusUpdate', onStatus);
    } else {
      player.replace({ uri });
    }
    return player;
  }

  function loadChapter(index: number, positionMs: number, autoplay: boolean) {
    const { book } = get();
    if (!book) return;
    const clamped = Math.max(0, Math.min(index, book.chapters.length - 1));
    const chapter = book.chapters[clamped];
    transitioning = true;
    const p = ensurePlayer(chapter.uri);
    set({ chapterIndex: clamped, positionMs, durationMs: chapter.durationMs });
    if (positionMs > 0) p.seekTo(positionMs / 1000);
    p.setPlaybackRate(get().rate, 'high');
    if (player === p && (autoplay || get().playing)) activateLockScreen();
    if (autoplay) {
      lastInternalActionAt = Date.now();
      p.play();
      playingSince = Date.now();
      set({ playing: true });
    }
    repo.savePosition(book.id, clamped, positionMs);
    setTimeout(() => {
      transitioning = false;
    }, EXTERNAL_GUARD_MS);
  }

  // ---- shake-to-reset: after a sleep timer fires, a shake within 30s resumes ----
  function armShakeWindow() {
    set({ awaitingShake: true });
    let last = { x: 0, y: 0, z: 0 };
    Accelerometer.setUpdateInterval(200);
    shakeSub = Accelerometer.addListener(({ x, y, z }) => {
      const delta = Math.abs(x - last.x) + Math.abs(y - last.y) + Math.abs(z - last.z);
      last = { x, y, z };
      if (delta > SHAKE_THRESHOLD) {
        disarmShake();
        get().play();
        get().setSleep(get().sleepMode); // re-arm same mode if it was timed
      }
    });
    setTimeout(disarmShake, SHAKE_WINDOW_MS);
  }
  function disarmShake() {
    shakeSub?.remove();
    shakeSub = null;
    set({ awaitingShake: false });
  }

  function clearSleepTimers() {
    if (sleepTimeout) clearTimeout(sleepTimeout);
    if (sleepTick) clearInterval(sleepTick);
    sleepTimeout = null;
    sleepTick = null;
  }

  return {
    book: null,
    chapterIndex: 0,
    playing: false,
    positionMs: 0,
    durationMs: 0,
    rate: 1,
    sleepMode: null,
    sleepRemainingMs: null,
    awaitingShake: false,

    loadBook(book, chapterIndex, positionMs, autoplay = true) {
      const sameBook = get().book?.id === book.id;
      set({ book });
      if (!sameBook) {
        set({ rate: settingsSnapshot().defaultRate });
        loadChapter(chapterIndex ?? book.currentChapterIndex, positionMs ?? book.positionInChapterMs, autoplay);
      } else if (autoplay && !get().playing) {
        get().play();
      }
    },

    play() {
      if (!player) return;
      lastInternalActionAt = Date.now();
      activateLockScreen();
      player.play();
      handlePlayStateChange(true);
    },

    pause() {
      if (!player) return;
      lastInternalActionAt = Date.now();
      player.pause();
      handlePlayStateChange(false);
    },

    togglePlay() {
      get().playing ? get().pause() : get().play();
    },

    seekToMs(ms, recordJump = true) {
      if (!player) return;
      const clamped = Math.max(0, Math.min(ms, get().durationMs));
      player.seekTo(clamped / 1000);
      set({ positionMs: clamped });
      if (recordJump) record('Jumped', consumeListened(true));
      persist();
    },

    rewind() {
      get().seekToMs(get().positionMs - settingsSnapshot().skipSeconds * 1000);
    },
    fastForward() {
      get().seekToMs(get().positionMs + settingsSnapshot().skipSeconds * 1000);
    },

    nextChapter(auto = false) {
      const { book, chapterIndex } = get();
      if (!book) return;
      const listened = consumeListened(true);
      if (chapterIndex + 1 < book.chapters.length) {
        record(auto ? 'NewChapter' : 'SkippedToChapter', listened);
        loadChapter(chapterIndex + 1, 0, true);
      } else {
        record('Paused', listened);
        lastInternalActionAt = Date.now();
        player?.pause();
        playingSince = null;
        set({ playing: false });
      }
    },

    prevChapter() {
      const { book, chapterIndex, positionMs } = get();
      if (!book) return;
      if (positionMs > 3000 || chapterIndex === 0) {
        get().seekToMs(0);
        return;
      }
      record('SkippedToChapter', consumeListened(true));
      loadChapter(chapterIndex - 1, 0, true);
    },

    jumpToChapter(index) {
      const { book, chapterIndex } = get();
      if (!book || index === chapterIndex) return;
      record('SkippedToChapter', consumeListened(true));
      loadChapter(index, 0, true);
    },

    setRate(rate) {
      set({ rate });
      player?.setPlaybackRate(rate, 'high');
    },

    setSleep(mode) {
      clearSleepTimers();
      disarmShake();
      if (mode == null) {
        set({ sleepMode: null, sleepRemainingMs: null });
        return;
      }
      if (mode === 'endOfChapter') {
        set({ sleepMode: 'endOfChapter', sleepRemainingMs: null });
        return;
      }
      let remaining = mode * 60 * 1000;
      set({ sleepMode: mode, sleepRemainingMs: remaining });
      sleepTick = setInterval(() => {
        remaining -= 1000;
        set({ sleepRemainingMs: Math.max(0, remaining) });
        if (remaining <= 0) clearSleepTimers();
      }, 1000);
      sleepTimeout = setTimeout(() => {
        clearSleepTimers();
        const listened = consumeListened(false);
        record('SleepTimer', listened);
        set({ sleepMode: null, sleepRemainingMs: null });
        suppressNextPause = true;
        get().pause();
        armShakeWindow();
      }, mode * 60 * 1000);
    },
  };
});

export async function initAudioMode() {
  await setAudioModeAsync({
    playsInSilentMode: true,
    shouldPlayInBackground: true,
    interruptionMode: 'doNotMix',
  });
}

export function releasePlayer() {
  statusSub?.remove();
  shakeSub?.remove();
  player?.clearLockScreenControls();
  player?.remove();
  player = null;
  statusSub = null;
  shakeSub = null;
}
