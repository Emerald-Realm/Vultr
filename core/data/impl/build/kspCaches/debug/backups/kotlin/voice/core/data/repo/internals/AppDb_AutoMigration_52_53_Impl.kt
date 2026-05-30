package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_52_53_Impl : Migration {
  public constructor() : super(52, 53)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_chapters2` (`id` TEXT NOT NULL, `name` TEXT, `duration` INTEGER NOT NULL, `fileLastModified` TEXT NOT NULL, `markData` TEXT NOT NULL, PRIMARY KEY(`id`))")
    connection.execSQL("INSERT INTO `_new_chapters2` (`id`,`name`,`duration`,`fileLastModified`,`markData`) SELECT `id`,`name`,`duration`,`fileLastModified`,`markData` FROM `chapters2`")
    connection.execSQL("DROP TABLE `chapters2`")
    connection.execSQL("ALTER TABLE `_new_chapters2` RENAME TO `chapters2`")
  }
}
