import { StorageAccessFramework as SAF, getInfoAsync } from 'expo-file-system/legacy';
import { createAudioPlayer } from 'expo-audio';
import { repo } from '../db/repo';

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

// ---- optional native metadata (graceful fallback to filename-based naming) ----
type FileMeta = { title?: string; artist?: string; album?: string; track?: number | null };

async function readMeta(uri: string): Promise<FileMeta> {
  try {
    const lib = require('@missingcore/react-native-metadata-retriever');
    const m = await lib.getMetadata(uri, ['title', 'artist', 'albumArtist', 'albumTitle', 'trackNumber']);
    return {
      title: m.title ?? undefined,
      artist: m.artist ?? m.albumArtist ?? undefined,
      album: m.albumTitle ?? undefined,
      track: m.trackNumber ?? null,
    };
  } catch {
    return {};
  }
}

async function saveArtwork(uri: string): Promise<string | null> {
  try {
    const lib = require('@missingcore/react-native-metadata-retriever');
    return (await lib.saveArtwork(uri)) ?? null;
  } catch {
    return null;
  }
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
  // metadata-aware ordering: prefer track numbers, else natural filename order
  const withMeta = await Promise.all(
    audio.map(async (uri) => ({ uri, meta: await readMeta(uri) })),
  );
  withMeta.sort((a, b) => {
    const ta = a.meta.track ?? null;
    const tb = b.meta.track ?? null;
    if (ta != null && tb != null) return ta - tb;
    return byName(a.uri, b.uri);
  });

  const bookTitle = withMeta[0]?.meta.album ?? fallbackTitle;
  const author = authorOverride ?? withMeta[0]?.meta.artist ?? null;
  const cover = (await saveArtwork(withMeta[0].uri)) ?? folderCover;

  const chapters: { name: string; durationMs: number; uri: string }[] = [];
  for (let i = 0; i < withMeta.length; i++) {
    onProbe(bookTitle, i + 1, withMeta.length);
    const durationMs = await probeDurationMs(withMeta[i].uri);
    chapters.push({
      name: withMeta[i].meta.title ?? stripExt(decodedName(withMeta[i].uri)),
      durationMs,
      uri: withMeta[i].uri,
    });
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
