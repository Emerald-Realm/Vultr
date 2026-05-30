package voice.core.`data`.repo.internals

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.FtsTableInfo
import androidx.room.util.TableInfo
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass
import voice.core.`data`.repo.internals.dao.BookContentDao
import voice.core.`data`.repo.internals.dao.BookContentDao_Impl
import voice.core.`data`.repo.internals.dao.BookmarkDao
import voice.core.`data`.repo.internals.dao.BookmarkDao_Impl
import voice.core.`data`.repo.internals.dao.ChapterDao
import voice.core.`data`.repo.internals.dao.ChapterDao_Impl
import voice.core.`data`.repo.internals.dao.ListeningSessionDao
import voice.core.`data`.repo.internals.dao.ListeningSessionDao_Impl
import voice.core.`data`.repo.internals.dao.RecentBookSearchDao
import voice.core.`data`.repo.internals.dao.RecentBookSearchDao_Impl
import androidx.room.util.FtsTableInfo.Companion.read as ftsTableInfoRead
import androidx.room.util.TableInfo.Companion.read as tableInfoRead

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDb_Impl : AppDb() {
  private val _chapterDao: Lazy<ChapterDao> = lazy {
    ChapterDao_Impl(this)
  }

  private val _bookContentDao: Lazy<BookContentDao> = lazy {
    BookContentDao_Impl(this)
  }

  private val _bookmarkDao: Lazy<BookmarkDao> = lazy {
    BookmarkDao_Impl(this)
  }

  private val _recentBookSearchDao: Lazy<RecentBookSearchDao> = lazy {
    RecentBookSearchDao_Impl(this)
  }

  private val _listeningSessionDao: Lazy<ListeningSessionDao> = lazy {
    ListeningSessionDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(61, "4e1bb72888341d538656360fc02bbe90", "b9d85b79caecf56ad2101957f566ea26") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chapters2` (`id` TEXT NOT NULL, `name` TEXT, `duration` INTEGER NOT NULL, `fileLastModified` TEXT NOT NULL, `markData` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `content2` (`id` TEXT NOT NULL, `playbackSpeed` REAL NOT NULL, `skipSilence` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `lastPlayedAt` TEXT NOT NULL, `author` TEXT, `name` TEXT NOT NULL, `addedAt` TEXT NOT NULL, `chapters` TEXT NOT NULL, `currentChapter` TEXT NOT NULL, `positionInChapter` INTEGER NOT NULL, `cover` TEXT, `gain` REAL NOT NULL DEFAULT 0, `genre` TEXT, `narrator` TEXT, `series` TEXT, `part` TEXT, `description` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `bookmark2` (`bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `title` TEXT, `time` INTEGER NOT NULL, `addedAt` TEXT NOT NULL, `setBySleepTimer` INTEGER NOT NULL, `id` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `bookSearchFts` USING FTS4(`name` TEXT NOT NULL, `author` TEXT, `genre` TEXT, `narrator` TEXT, `series` TEXT, `part` TEXT, `description` TEXT, `id` TEXT NOT NULL, `isActive` INTEGER NOT NULL, tokenize=unicode61, content=`content2`, notindexed=`id`, notindexed=`isActive`)")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_BEFORE_UPDATE BEFORE UPDATE ON `content2` BEGIN DELETE FROM `bookSearchFts` WHERE `docid`=OLD.`rowid`; END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_BEFORE_DELETE BEFORE DELETE ON `content2` BEGIN DELETE FROM `bookSearchFts` WHERE `docid`=OLD.`rowid`; END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_AFTER_UPDATE AFTER UPDATE ON `content2` BEGIN INSERT INTO `bookSearchFts`(`docid`, `name`, `author`, `genre`, `narrator`, `series`, `part`, `description`, `id`, `isActive`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`author`, NEW.`genre`, NEW.`narrator`, NEW.`series`, NEW.`part`, NEW.`description`, NEW.`id`, NEW.`isActive`); END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_AFTER_INSERT AFTER INSERT ON `content2` BEGIN INSERT INTO `bookSearchFts`(`docid`, `name`, `author`, `genre`, `narrator`, `series`, `part`, `description`, `id`, `isActive`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`author`, NEW.`genre`, NEW.`narrator`, NEW.`series`, NEW.`part`, NEW.`description`, NEW.`id`, NEW.`isActive`); END")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recentBookSearch` (`searchTerm` TEXT NOT NULL, PRIMARY KEY(`searchTerm`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `listening_session` (`id` TEXT NOT NULL, `bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `action` TEXT NOT NULL DEFAULT 'PAUSED', `positionInChapter` INTEGER NOT NULL DEFAULT 0, `createdAt` TEXT NOT NULL DEFAULT '1970-01-01T00:00:00Z', `listenedMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4e1bb72888341d538656360fc02bbe90')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `chapters2`")
        connection.execSQL("DROP TABLE IF EXISTS `content2`")
        connection.execSQL("DROP TABLE IF EXISTS `bookmark2`")
        connection.execSQL("DROP TABLE IF EXISTS `bookSearchFts`")
        connection.execSQL("DROP TABLE IF EXISTS `recentBookSearch`")
        connection.execSQL("DROP TABLE IF EXISTS `listening_session`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_BEFORE_UPDATE BEFORE UPDATE ON `content2` BEGIN DELETE FROM `bookSearchFts` WHERE `docid`=OLD.`rowid`; END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_BEFORE_DELETE BEFORE DELETE ON `content2` BEGIN DELETE FROM `bookSearchFts` WHERE `docid`=OLD.`rowid`; END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_AFTER_UPDATE AFTER UPDATE ON `content2` BEGIN INSERT INTO `bookSearchFts`(`docid`, `name`, `author`, `genre`, `narrator`, `series`, `part`, `description`, `id`, `isActive`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`author`, NEW.`genre`, NEW.`narrator`, NEW.`series`, NEW.`part`, NEW.`description`, NEW.`id`, NEW.`isActive`); END")
        connection.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_bookSearchFts_AFTER_INSERT AFTER INSERT ON `content2` BEGIN INSERT INTO `bookSearchFts`(`docid`, `name`, `author`, `genre`, `narrator`, `series`, `part`, `description`, `id`, `isActive`) VALUES (NEW.`rowid`, NEW.`name`, NEW.`author`, NEW.`genre`, NEW.`narrator`, NEW.`series`, NEW.`part`, NEW.`description`, NEW.`id`, NEW.`isActive`); END")
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsChapters2: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChapters2.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters2.put("name", TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters2.put("duration", TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters2.put("fileLastModified", TableInfo.Column("fileLastModified", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChapters2.put("markData", TableInfo.Column("markData", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChapters2: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChapters2: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChapters2: TableInfo = TableInfo("chapters2", _columnsChapters2, _foreignKeysChapters2, _indicesChapters2)
        val _existingChapters2: TableInfo = tableInfoRead(connection, "chapters2")
        if (!_infoChapters2.equals(_existingChapters2)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chapters2(voice.core.data.Chapter).
              | Expected:
              |""".trimMargin() + _infoChapters2 + """
              |
              | Found:
              |""".trimMargin() + _existingChapters2)
        }
        val _columnsContent2: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsContent2.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("playbackSpeed", TableInfo.Column("playbackSpeed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("skipSilence", TableInfo.Column("skipSilence", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("lastPlayedAt", TableInfo.Column("lastPlayedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("author", TableInfo.Column("author", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("addedAt", TableInfo.Column("addedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("chapters", TableInfo.Column("chapters", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("currentChapter", TableInfo.Column("currentChapter", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("positionInChapter", TableInfo.Column("positionInChapter", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("cover", TableInfo.Column("cover", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("gain", TableInfo.Column("gain", "REAL", true, 0, "0", TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("genre", TableInfo.Column("genre", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("narrator", TableInfo.Column("narrator", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("series", TableInfo.Column("series", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("part", TableInfo.Column("part", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContent2.put("description", TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysContent2: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesContent2: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoContent2: TableInfo = TableInfo("content2", _columnsContent2, _foreignKeysContent2, _indicesContent2)
        val _existingContent2: TableInfo = tableInfoRead(connection, "content2")
        if (!_infoContent2.equals(_existingContent2)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |content2(voice.core.data.BookContent).
              | Expected:
              |""".trimMargin() + _infoContent2 + """
              |
              | Found:
              |""".trimMargin() + _existingContent2)
        }
        val _columnsBookmark2: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBookmark2.put("bookId", TableInfo.Column("bookId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("chapterId", TableInfo.Column("chapterId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("title", TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("time", TableInfo.Column("time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("addedAt", TableInfo.Column("addedAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("setBySleepTimer", TableInfo.Column("setBySleepTimer", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBookmark2.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBookmark2: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBookmark2: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBookmark2: TableInfo = TableInfo("bookmark2", _columnsBookmark2, _foreignKeysBookmark2, _indicesBookmark2)
        val _existingBookmark2: TableInfo = tableInfoRead(connection, "bookmark2")
        if (!_infoBookmark2.equals(_existingBookmark2)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bookmark2(voice.core.data.Bookmark).
              | Expected:
              |""".trimMargin() + _infoBookmark2 + """
              |
              | Found:
              |""".trimMargin() + _existingBookmark2)
        }
        val _columnsBookSearchFts: MutableSet<String> = mutableSetOf()
        _columnsBookSearchFts.add("name")
        _columnsBookSearchFts.add("author")
        _columnsBookSearchFts.add("genre")
        _columnsBookSearchFts.add("narrator")
        _columnsBookSearchFts.add("series")
        _columnsBookSearchFts.add("part")
        _columnsBookSearchFts.add("description")
        _columnsBookSearchFts.add("id")
        _columnsBookSearchFts.add("isActive")
        val _infoBookSearchFts: FtsTableInfo = FtsTableInfo("bookSearchFts", _columnsBookSearchFts, "CREATE VIRTUAL TABLE IF NOT EXISTS `bookSearchFts` USING FTS4(`name` TEXT NOT NULL, `author` TEXT, `genre` TEXT, `narrator` TEXT, `series` TEXT, `part` TEXT, `description` TEXT, `id` TEXT NOT NULL, `isActive` INTEGER NOT NULL, tokenize=unicode61, content=`content2`, notindexed=`id`, notindexed=`isActive`)")
        val _existingBookSearchFts: FtsTableInfo = ftsTableInfoRead(connection, "bookSearchFts")
        if (!_infoBookSearchFts.equals(_existingBookSearchFts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bookSearchFts(voice.core.data.repo.internals.dao.BookSearchFts).
              | Expected:
              |""".trimMargin() + _infoBookSearchFts + """
              |
              | Found:
              |""".trimMargin() + _existingBookSearchFts)
        }
        val _columnsRecentBookSearch: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecentBookSearch.put("searchTerm", TableInfo.Column("searchTerm", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecentBookSearch: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRecentBookSearch: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRecentBookSearch: TableInfo = TableInfo("recentBookSearch", _columnsRecentBookSearch, _foreignKeysRecentBookSearch, _indicesRecentBookSearch)
        val _existingRecentBookSearch: TableInfo = tableInfoRead(connection, "recentBookSearch")
        if (!_infoRecentBookSearch.equals(_existingRecentBookSearch)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recentBookSearch(voice.core.data.RecentBookSearch).
              | Expected:
              |""".trimMargin() + _infoRecentBookSearch + """
              |
              | Found:
              |""".trimMargin() + _existingRecentBookSearch)
        }
        val _columnsListeningSession: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsListeningSession.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("bookId", TableInfo.Column("bookId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("chapterId", TableInfo.Column("chapterId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("action", TableInfo.Column("action", "TEXT", true, 0, "'PAUSED'", TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("positionInChapter", TableInfo.Column("positionInChapter", "INTEGER", true, 0, "0", TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("createdAt", TableInfo.Column("createdAt", "TEXT", true, 0, "'1970-01-01T00:00:00Z'", TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningSession.put("listenedMs", TableInfo.Column("listenedMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysListeningSession: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesListeningSession: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoListeningSession: TableInfo = TableInfo("listening_session", _columnsListeningSession, _foreignKeysListeningSession, _indicesListeningSession)
        val _existingListeningSession: TableInfo = tableInfoRead(connection, "listening_session")
        if (!_infoListeningSession.equals(_existingListeningSession)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |listening_session(voice.core.data.ListeningSession).
              | Expected:
              |""".trimMargin() + _infoListeningSession + """
              |
              | Found:
              |""".trimMargin() + _existingListeningSession)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    _shadowTablesMap.put("bookSearchFts", "content2")
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "chapters2", "content2", "bookmark2", "bookSearchFts", "recentBookSearch", "listening_session")
  }

  public override fun clearAllTables() {
    super.performClear(false, "chapters2", "content2", "bookmark2", "bookSearchFts", "recentBookSearch", "listening_session")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ChapterDao::class, ChapterDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BookContentDao::class, BookContentDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BookmarkDao::class, BookmarkDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RecentBookSearchDao::class, RecentBookSearchDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ListeningSessionDao::class, ListeningSessionDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    _autoMigrations.add(AppDb_AutoMigration_51_52_Impl())
    _autoMigrations.add(AppDb_AutoMigration_52_53_Impl())
    _autoMigrations.add(AppDb_AutoMigration_54_55_Impl())
    _autoMigrations.add(AppDb_AutoMigration_55_56_Impl())
    _autoMigrations.add(AppDb_AutoMigration_56_57_Impl())
    _autoMigrations.add(AppDb_AutoMigration_57_58_Impl())
    _autoMigrations.add(AppDb_AutoMigration_58_59_Impl())
    _autoMigrations.add(AppDb_AutoMigration_59_60_Impl())
    _autoMigrations.add(AppDb_AutoMigration_60_61_Impl())
    return _autoMigrations
  }

  public override fun chapterDao(): ChapterDao = _chapterDao.value

  public override fun bookContentDao(): BookContentDao = _bookContentDao.value

  public override fun bookmarkDao(): BookmarkDao = _bookmarkDao.value

  public override fun recentBookSearchDao(): RecentBookSearchDao = _recentBookSearchDao.value

  public override fun listeningSessionDao(): ListeningSessionDao = _listeningSessionDao.value
}
