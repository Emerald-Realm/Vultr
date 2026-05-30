import { StorageAccessFramework as SAF, getInfoAsync } from 'expo-file-system/legacy';
import { createAudioPlayer } from 'expo-audio';
import { repo } from '../db/repo';
import { getMetadata, getChapters } from '../../modules/audio-metadata';

const AUDIO_EXT = ['mp3', 'm4a', 'm4b', 'aac', 'ogg', 'oga', 'opus', 'wav', 'flac', 'mp4', '3gp'];
const IMAGE_EXT = ['jpg', 'jpeg', 'png', 'webp'];

export type FolderMode = 'single' | 'topLevel' | 'author';

export interface ScanProgress {
  phase: 'picking' | 'scanning' | 'probing' | 'done' | 'cancelled';
  message?: string;
  current?: number;
  total?: number;
}

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));

function decodedName(uri: string): string {
  const decoded = decodeURIComponent(uri);
  const afterColon = decoded.includes(':') ? decoded.slice(decoded.lastIndexOf(':') + 1) : decoded;
  return afterColon.split('/').filter(Boolean).pop() ?? decoded;
}
function ext(uri: string): string {
  const name = decodedName(uri).toLowerCase();
  const dot = name.lastIndexOf('.');
  return dot >= 0 ? name.slice(dot + 1) : '';
}
function stripExt(name: string): string {
  const dot = name.lastIndexOf('.');
  return dot > 0 ? name.slice(0, dot) : name;
}
function byName(a: string, b: string): number {
  return decodedName(a).localeCompare(decodedName(b), undefined, { numeric: true, sensitivity: 'base' });
}

// Embedded tags + cover art are read by the local `audio-metadata` native module
// (framework MediaMetadataRetriever — no Media3 dependency). Falls back to names.
type FileMeta = {
  title?: string;
  artist?: string;
  album?: string;
  artworkUri?: string | null;
  durationMs?: number;
};

async function readMeta(uri: string): Promise<FileMeta> {
  const m = await getMetadata(uri);
  if (!m) return {};
  return {
    title: m.title ?? undefined,
    artist: m.artist ?? undefined,
    album: m.album ?? undefined,
    artworkUri: m.artworkUri,
    durationMs: m.durationMs ?? undefined,
  };
}

async function probeDurationMs(uri: string): Promise<number> {
  const p = createAudioPlayer({ uri }, { updateInterval: 200 });
  try {
    const start = Date.now();
    while (Date.now() - start < 6000) {
      const st = p.currentStatus;
      if (st?.isLoaded && st.duration > 0) return Math.round(st.duration * 1000);
      await delay(150);
    }
    return 0;
  } catch {
    return 0;
  } finally {
    try {
      p.remove();
    } catch {}
  }
}

type FolderContents = { audio: string[]; images: string[]; dirs: string[] };

async function classify(dirUri: string): Promise<FolderContents> {
  const children = await SAF.readDirectoryAsync(dirUri);
  const audio: string[] = [];
  const images: string[] = [];
  const dirs: string[] = [];
  for (const c of children) {
    const e = ext(c);
    if (AUDIO_EXT.includes(e)) audio.push(c);
    else if (IMAGE_EXT.includes(e)) images.push(c);
    else if (e === '') {
      try {
        const info = await getInfoAsync(c);
        if (info.exists && info.isDirectory) dirs.push(c);
      } catch {}
    }
  }
  return { audio, images, dirs };
}

async function buildBook(
  id: string,
  fallbackTitle: string,
  audio: string[],
  folderCover: string | null,
  authorOverride: string | null,
  onProbe: (label: string, i: number, total: number) => void,
): Promise<boolean> {
  const sorted = [...audio].sort(byName);
  const withMeta = await Promise.all(
    sorted.map(async (uri) => ({ uri, meta: await readMeta(uri) })),
  );

  const bookTitle = withMeta[0]?.meta.album ?? withMeta[0]?.meta.title ?? fallbackTitle;
  const author = authorOverride ?? withMeta[0]?.meta.artist ?? null;
  const cover = withMeta[0]?.meta.artworkUri ?? folderCover;

  type ChapterInput = { name: string; durationMs: number; uri: string; startMs: number };
  const chapters: ChapterInput[] = [];

  // Single audio file: prefer embedded chapter marks (e.g. M4B), splitting it in place.
  if (withMeta.length === 1) {
    const { uri, meta } = withMeta[0];
    onProbe(bookTitle, 1, 1);
    const fileDurationMs = meta.durationMs ?? (await probeDurationMs(uri));
    const marks = (await getChapters(uri)).filter((c) => c.startMs >= 0).sort((a, b) => a.startMs - b.startMs);
    if (marks.length > 1) {
      marks.forEach((m, i) => {
        const end = i + 1 < marks.length ? marks[i + 1].startMs : fileDurationMs;
        chapters.push({
          name: m.title?.trim() || `Chapter ${i + 1}`,
          durationMs: Math.max(0, end - m.startMs),
          uri,
          startMs: m.startMs,
        });
      });
    } else {
      chapters.push({
        name: meta.title ?? stripExt(decodedName(uri)),
        durationMs: fileDurationMs,
        uri,
        startMs: 0,
      });
    }
  } else {
    // Multiple files: each file is a chapter starting at offset 0 within itself.
    for (let i = 0; i < withMeta.length; i++) {
      onProbe(bookTitle, i + 1, withMeta.length);
      const { uri, meta } = withMeta[i];
      const durationMs = meta.durationMs ?? (await probeDurationMs(uri));
      chapters.push({
        name: meta.title ?? stripExt(decodedName(uri)),
        durationMs,
        uri,
        startMs: 0,
      });
    }
  }

  repo.insertBook({ id, title: bookTitle, author, coverUri: cover, chapters });
  return chapters.length > 0;
}

/**
 * Prompts for a folder via SAF and imports audiobooks using the chosen structure:
 * - single: the picked folder is one book (files = chapters)
 * - topLevel: each subfolder is a book
 * - author: subfolders are authors, sub-subfolders are books
 */
export async function importFromFolder(
  mode: FolderMode,
  onProgress: (p: ScanProgress) => void,
): Promise<number> {
  onProgress({ phase: 'picking' });
  const perm = await SAF.requestDirectoryPermissionsAsync();
  if (!perm.granted) {
    onProgress({ phase: 'cancelled' });
    return 0;
  }
  const rootUri = perm.directoryUri;
  onProgress({ phase: 'scanning', message: 'Reading folder…' });
  const root = await classify(rootUri);
  let imported = 0;
  const probe = (label: string, current: number, total: number) =>
    onProgress({ phase: 'probing', message: label, current, total });

  if (mode === 'single') {
    if (root.audio.length > 0 && (await buildBook(rootUri, decodedName(rootUri), root.audio, root.images.sort(byName)[0] ?? null, null, probe))) {
      imported++;
    }
  } else if (mode === 'topLevel') {
    const dirs = root.audio.length > 0 ? [rootUri] : root.dirs.sort(byName);
    for (const dir of dirs) {
      const sub = dir === rootUri ? root : await classify(dir);
      if (sub.audio.length === 0) continue;
      if (await buildBook(dir, decodedName(dir), sub.audio, sub.images.sort(byName)[0] ?? null, null, probe)) imported++;
    }
  } else {
    // author mode
    for (const authorDir of root.dirs.sort(byName)) {
      const authorName = decodedName(authorDir);
      const author = await classify(authorDir);
      for (const bookDir of author.dirs.sort(byName)) {
        const sub = await classify(bookDir);
        if (sub.audio.length === 0) continue;
        if (await buildBook(bookDir, decodedName(bookDir), sub.audio, sub.images.sort(byName)[0] ?? null, authorName, probe)) imported++;
      }
    }
  }

  onProgress({ phase: 'done', total: imported });
  return imported;
}
