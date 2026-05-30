package voice.core.data.repo.internals.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = "listening_session", columnName = "startedAt")
@DeleteColumn(tableName = "listening_session", columnName = "endedAt")
@DeleteColumn(tableName = "listening_session", columnName = "startPositionInChapter")
@DeleteColumn(tableName = "listening_session", columnName = "endPositionInChapter")
internal class ListeningSessionMigration60 : AutoMigrationSpec
