package voice.core.`data`.repo.internals

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_55_56_Impl : Migration {
  public constructor() : super(55, 56)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `bookSearchFts` USING FTS4(`name` TEXT NOT NULL, `author` TEXT, `id` TEXT NOT NULL, `isActive` INTEGER NOT NULL, tokenize=unicode61, content=`content2`, notindexed=`id`, notindexed=`isActive`)")
    connection.execSQL("CREATE TABLE IF NOT EXISTS `recentBookSearch` (`searchTerm` TEXT NOT NULL, PRIMARY KEY(`searchTerm`))")
  }
}
