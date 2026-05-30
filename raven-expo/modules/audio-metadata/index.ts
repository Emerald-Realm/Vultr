import { requireNativeModule } from 'expo-modules-core';

export type TrackMetadata = {
  title: string | null;
  artist: string | null;
  album: string | null;
  durationMs: number | null;
  artworkUri: string | null;
};

export type EmbeddedChapter = {
  title: string | null;
  startMs: number;
};

let _native: any | undefined;
function native(): any | null {
  if (_native === undefined) {
    try {
      _native = requireNativeModule('AudioMetadata');
    } catch {
      _native = null;
    }
  }
  return _native;
}

/** Reads embedded tags + cover art + duration via Android MediaMetadataRetriever. */
export async function getMetadata(uri: string): Promise<TrackMetadata | null> {
  try {
    return (await native()?.getMetadata(uri)) ?? null;
  } catch {
    return null;
  }
}

/** Returns embedded chapter marks (Nero `chpl`) for a single audio file, or [] if none. */
export async function getChapters(uri: string): Promise<EmbeddedChapter[]> {
  try {
    return (await native()?.getChapters(uri)) ?? [];
  } catch {
    return [];
  }
}

export default { getMetadata, getChapters };
