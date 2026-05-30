import { Book, Chapter, HistoryEvent } from '../data/types';
import { getDb } from './database';

type BookRow = {
  id: string;
  title: string;
  author: string | null;
  coverUri: string | null;
  description: string | null;
  currentChapterIndex: number;
  positionInChapterMs: number;
  lastPlayedAt: number | null;
};

function chaptersFor(bookId: string): Chapter[] {
  return getDb().getAllSync<Chapter & { sortIndex: number }>(
    'SELECT id, bookId, name, durationMs, uri, startMs FROM chapters WHERE bookId = ? ORDER BY sortIndex ASC',
    bookId,
  );
}

function hydrate(row: BookRow): Book {
  return { ...row, chapters: chaptersFor(row.id) };
}

export const repo = {
  allBooks(): Book[] {
    const rows = getDb().getAllSync<BookRow>('SELECT * FROM books');
    return rows.map(hydrate);
  },

  getBook(id: string): Book | null {
    const row = getDb().getFirstSync<BookRow>('SELECT * FROM books WHERE id = ?', id);
    return row ? hydrate(row) : null;
  },

  savePosition(bookId: string, chapterIndex: number, positionInChapterMs: number) {
    getDb().runSync(
      'UPDATE books SET currentChapterIndex = ?, positionInChapterMs = ?, lastPlayedAt = ? WHERE id = ?',
      chapterIndex,
      Math.round(positionInChapterMs),
      Date.now(),
      bookId,
    );
  },

  addHistory(e: Omit<HistoryEvent, 'id'>) {
    getDb().runSync(
      `INSERT INTO history (id, bookId, chapterId, action, positionInChapterMs, createdAt, listenedMs)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      `h-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      e.bookId,
      e.chapterId,
      e.action,
      Math.round(e.positionInChapterMs),
      e.createdAt,
      Math.round(e.listenedMs),
    );
  },

  historyFor(bookId: string): HistoryEvent[] {
    return getDb().getAllSync<HistoryEvent>(
      'SELECT * FROM history WHERE bookId = ? ORDER BY createdAt DESC',
      bookId,
    );
  },

  deleteHistory(id: string) {
    getDb().runSync('DELETE FROM history WHERE id = ?', id);
  },

  bookExists(id: string): boolean {
    return !!getDb().getFirstSync<{ id: string }>('SELECT id FROM books WHERE id = ?', id);
  },

  // Insert (or replace) an imported book and its chapters. Preserves an existing
  // reading position if the book was imported before.
  insertBook(input: {
    id: string;
    title: string;
    author: string | null;
    coverUri: string | null;
    chapters: { name: string; durationMs: number; uri: string; startMs?: number }[];
  }) {
    const db = getDb();
    const existing = repo.getBook(input.id);
    db.withTransactionSync(() => {
      db.runSync(
        `INSERT OR REPLACE INTO books (id, title, author, coverUri, description, currentChapterIndex, positionInChapterMs, lastPlayedAt)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
        input.id,
        input.title,
        input.author,
        input.coverUri,
        existing?.description ?? null,
        existing?.currentChapterIndex ?? 0,
        existing?.positionInChapterMs ?? 0,
        existing?.lastPlayedAt ?? null,
      );
      db.runSync('DELETE FROM chapters WHERE bookId = ?', input.id);
      input.chapters.forEach((c, i) => {
        db.runSync(
          `INSERT INTO chapters (id, bookId, name, durationMs, uri, sortIndex, startMs) VALUES (?, ?, ?, ?, ?, ?, ?)`,
          `${input.id}::${i}`,
          input.id,
          c.name,
          c.durationMs,
          c.uri,
          i,
          Math.round(c.startMs ?? 0),
        );
      });
    });
  },

  updateTitle(id: string, title: string) {
    getDb().runSync('UPDATE books SET title = ? WHERE id = ?', title, id);
  },

  deleteBook(id: string) {
    const db = getDb();
    db.withTransactionSync(() => {
      db.runSync('DELETE FROM chapters WHERE bookId = ?', id);
      db.runSync('DELETE FROM history WHERE bookId = ?', id);
      db.runSync('DELETE FROM books WHERE id = ?', id);
    });
  },
};
