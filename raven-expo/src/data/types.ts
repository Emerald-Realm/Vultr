export interface Chapter {
  id: string;
  bookId: string;
  name: string;
  durationMs: number;
  uri: string;
  // Offset of this chapter within its source file. 0 for multi-file books;
  // non-zero for embedded chapters that share a single M4B file.
  startMs: number;
}

export interface Book {
  id: string;
  title: string;
  author: string | null;
  coverUri: string | null;
  description: string | null;
  currentChapterIndex: number;
  positionInChapterMs: number;
  lastPlayedAt: number | null;
  chapters: Chapter[];
}

export type HistoryAction =
  | 'Played'
  | 'Paused'
  | 'Jumped'
  | 'SkippedToChapter'
  | 'NewChapter'
  | 'SleepTimer';

export interface HistoryEvent {
  id: string;
  bookId: string;
  chapterId: string;
  action: HistoryAction;
  positionInChapterMs: number;
  createdAt: number;
  listenedMs: number;
}

export function bookDurationMs(book: Book): number {
  return book.chapters.reduce((sum, c) => sum + c.durationMs, 0);
}

export function bookPositionMs(book: Book): number {
  const prior = book.chapters
    .slice(0, book.currentChapterIndex)
    .reduce((sum, c) => sum + c.durationMs, 0);
  return prior + book.positionInChapterMs;
}

export function bookProgress(book: Book): number {
  const total = bookDurationMs(book);
  if (total <= 0) return 0;
  return Math.min(1, Math.max(0, bookPositionMs(book) / total));
}
