import { openDatabaseSync, SQLiteDatabase } from 'expo-sqlite';

let _db: SQLiteDatabase | null = null;

export function getDb(): SQLiteDatabase {
  if (_db) return _db;
  const db = openDatabaseSync('raven.db');
  db.execSync(`
    PRAGMA journal_mode = WAL;
    CREATE TABLE IF NOT EXISTS books (
      id TEXT PRIMARY KEY NOT NULL,
      title TEXT NOT NULL,
      author TEXT,
      coverUri TEXT,
      description TEXT,
      currentChapterIndex INTEGER NOT NULL DEFAULT 0,
      positionInChapterMs INTEGER NOT NULL DEFAULT 0,
      lastPlayedAt INTEGER
    );
    CREATE TABLE IF NOT EXISTS chapters (
      id TEXT PRIMARY KEY NOT NULL,
      bookId TEXT NOT NULL,
      name TEXT NOT NULL,
      durationMs INTEGER NOT NULL,
      uri TEXT NOT NULL,
      sortIndex INTEGER NOT NULL DEFAULT 0,
      startMs INTEGER NOT NULL DEFAULT 0
    );
    CREATE TABLE IF NOT EXISTS history (
      id TEXT PRIMARY KEY NOT NULL,
      bookId TEXT NOT NULL,
      chapterId TEXT NOT NULL,
      action TEXT NOT NULL,
      positionInChapterMs INTEGER NOT NULL,
      createdAt INTEGER NOT NULL,
      listenedMs INTEGER NOT NULL DEFAULT 0
    );
  `);
  // Migration: add chapters.startMs to databases created before embedded chapters.
  const cols = db.getAllSync<{ name: string }>('PRAGMA table_info(chapters)');
  if (!cols.some((c) => c.name === 'startMs')) {
    db.execSync('ALTER TABLE chapters ADD COLUMN startMs INTEGER NOT NULL DEFAULT 0');
  }
  _db = db;
  return db;
}
