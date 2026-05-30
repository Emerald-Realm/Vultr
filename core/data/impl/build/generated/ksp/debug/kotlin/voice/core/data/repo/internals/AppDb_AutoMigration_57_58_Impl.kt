package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_57_58_Impl : Migration {
  public constructor() : super(57, 58)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("ALTER TABLE `content2` ADD COLUMN `genre` TEXT DEFAULT NULL")
    connection.execSQL("ALTER TABLE `content2` ADD COLUMN `narrator` TEXT DEFAULT NULL")
    connection.execSQL("ALTER TABLE `content2` ADD COLUMN `series` TEXT DEFAULT NULL")
    connection.execSQL("ALTER TABLE `content2` ADD COLUMN `part` TEXT DEFAULT NULL")
    connection.execSQL("DROP TABLE `bookSearchFts`")
    connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `bookSearchFts` USING FTS4(`name` TEXT NOT NULL, `author` TEXT, `genre` TEXT, `narrator` TEXT, `series` TEXT, `part` TEXT, `id` TEXT NOT NULL, `isActive` INTEGER NOT NULL, tokenize=unicode61, content=`content2`, notindexed=`id`, notindexed=`isActive`)")
    connection.execSQL("INSERT INTO `bookSearchFts` (`name`,`author`,`id`,`isActive`,`docid`) SELECT `name`,`author`,`id`,`isActive`,`rowId` FROM `content2`")
  }
}
