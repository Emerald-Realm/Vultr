package voice.core.`data`.repo.internals

import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Suppress
import voice.core.`data`.repo.internals.migrations.Migration56

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
internal class AppDb_AutoMigration_56_57_Impl : Migration {
  private val callback: AutoMigrationSpec = Migration56()

  public constructor() : super(56, 57)

  public override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("DROP TABLE `bookmark`")
    connection.execSQL("DROP TABLE `chapters`")
    connection.execSQL("DROP TABLE `bookMetaData`")
    connection.execSQL("DROP TABLE `bookSettings`")
    callback.onPostMigrate(connection)
  }
}
