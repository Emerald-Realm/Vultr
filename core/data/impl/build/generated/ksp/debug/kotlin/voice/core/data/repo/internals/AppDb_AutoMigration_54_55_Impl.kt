package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_54_55_Impl : Migration {
  public constructor() : super(54, 55)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("ALTER TABLE `content2` ADD COLUMN `gain` REAL NOT NULL DEFAULT 0")
  }
}
