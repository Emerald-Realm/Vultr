package voice.core.`data`.repo.internals

import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress
import voice.core.`data`.repo.internals.migrations.ListeningSessionMigration60

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_60_61_Impl : Migration {
  private val callback: AutoMigrationSpec = ListeningSessionMigration60()

  public constructor() : super(60, 61)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("ALTER TABLE `listening_session` ADD COLUMN `action` TEXT NOT NULL DEFAULT 'PAUSED'")
    connection.execSQL("ALTER TABLE `listening_session` ADD COLUMN `positionInChapter` INTEGER NOT NULL DEFAULT 0")
    connection.execSQL("ALTER TABLE `listening_session` ADD COLUMN `createdAt` TEXT NOT NULL DEFAULT '1970-01-01T00:00:00Z'")
    connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_listening_session` (`id` TEXT NOT NULL, `bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `action` TEXT NOT NULL DEFAULT 'PAUSED', `positionInChapter` INTEGER NOT NULL DEFAULT 0, `createdAt` TEXT NOT NULL DEFAULT '1970-01-01T00:00:00Z', `listenedMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    connection.execSQL("INSERT INTO `_new_listening_session` (`id`,`bookId`,`chapterId`,`listenedMs`) SELECT `id`,`bookId`,`chapterId`,`listenedMs` FROM `listening_session`")
    connection.execSQL("DROP TABLE `listening_session`")
    connection.execSQL("ALTER TABLE `_new_listening_session` RENAME TO `listening_session`")
    callback.onPostMigrate(connection)
  }
}
