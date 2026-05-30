package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_59_60_Impl : Migration {
  public constructor() : super(59, 60)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("CREATE TABLE IF NOT EXISTS `listening_session` (`id` TEXT NOT NULL, `bookId` TEXT NOT NULL, `chapterId` TEXT NOT NULL, `startedAt` TEXT NOT NULL, `endedAt` TEXT NOT NULL, `startPositionInChapter` INTEGER NOT NULL, `endPositionInChapter` INTEGER NOT NULL, `listenedMs` INTEGER NOT NULL, PRIMARY KEY(`id`))")
  }
}
