package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_51_52_Impl : Migration {
  public constructor() : super(51, 52)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("CREATE TABLE IF NOT EXISTS `chapters2` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `duration` INTEGER NOT NULL, `fileLastModified` TEXT NOT NULL, `markData` TEXT NOT NULL, PRIMARY KEY(`id`))")
    connection.execSQL("CREATE TABLE IF NOT EXISTS `content2` (`id` TEXT NOT NULL, `playbackSpeed` REAL NOT NULL, `skipSilence` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `lastPlayedAt` TEXT NOT NULL, `author` TEXT, `name` TEXT NOT NULL, `addedAt` TEXT NOT NULL, `chapters` TEXT NOT NULL, `currentChapter` TEXT NOT NULL, `positionInChapter` INTEGER NOT NULL, `cover` TEXT, PRIMARY KEY(`id`))")
    connection.execSQL("CREATE TABLE IF NOT EXISTS `bookmark2` (`bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `title` TEXT, `time` INTEGER NOT NULL, `addedAt` TEXT NOT NULL, `setBySleepTimer` INTEGER NOT NULL, `id` TEXT NOT NULL, PRIMARY KEY(`id`))")
  }
}
