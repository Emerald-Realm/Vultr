import { getDb } from './database';

// Small public-domain sample clips so playback works in a dev build without
// any local files. Real folder-scanning import is a later milestone.
const SAMPLE = [
  'https://download.samplelib.com/mp3/sample-15s.mp3',
  'https://download.samplelib.com/mp3/sample-12s.mp3',
  'https://download.samplelib.com/mp3/sample-9s.mp3',
  'https://download.samplelib.com/mp3/sample-6s.mp3',
];

const DUR = [15000, 12000, 9000, 6000];

type SeedBook = {
  id: string;
  title: string;
  author: string;
  chapters: string[];
  currentChapterIndex?: number;
  positionInChapterMs?: number;
  lastPlayedAt?: number;
};

const BOOKS: SeedBook[] = [
  {
    id: 'b-sea-wolf',
    title: 'The Sea Wolf',
    author: 'Jack London',
    chapters: ['Chapter 1 — The Wreck', 'Chapter 2 — Aboard', 'Chapter 3 — The Captain'],
    currentChapterIndex: 1,
    positionInChapterMs: 4000,
    lastPlayedAt: Date.now() - 1000 * 60 * 30,
  },
  {
    id: 'b-meditations',
    title: 'Meditations',
    author: 'Marcus Aurelius',
    chapters: ['Book I', 'Book II', 'Book III', 'Book IV'],
  },
  {
    id: 'b-fables',
    title: "Aesop's Fables",
    author: 'Aesop',
    chapters: ['The Fox & the Grapes', 'The Tortoise & the Hare'],
    currentChapterIndex: 1,
    positionInChapterMs: DUR[1],
    lastPlayedAt: Date.now() - 1000 * 60 * 60 * 26,
  },
];

export function seedIfEmpty() {
  const db = getDb();
  const row = db.getFirstSync<{ c: number }>('SELECT COUNT(*) as c FROM books');
  if (row && row.c > 0) return;

  db.withTransactionSync(() => {
    for (const b of BOOKS) {
      db.runSync(
        `INSERT INTO books (id, title, author, coverUri, description, currentChapterIndex, positionInChapterMs, lastPlayedAt)
         VALUES (?, ?, ?, NULL, ?, ?, ?, ?)`,
        b.id,
        b.title,
        b.author,
        `${b.title} by ${b.author}. A sample audiobook bundled to demonstrate playback, chapters, progress and history.`,
        b.currentChapterIndex ?? 0,
        b.positionInChapterMs ?? 0,
        b.lastPlayedAt ?? null,
      );
      b.chapters.forEach((name, i) => {
        db.runSync(
          `INSERT INTO chapters (id, bookId, name, durationMs, uri, sortIndex)
           VALUES (?, ?, ?, ?, ?, ?)`,
          `${b.id}-c${i}`,
          b.id,
          name,
          DUR[i % DUR.length],
          SAMPLE[i % SAMPLE.length],
          i,
        );
      });
    }
  });
}
