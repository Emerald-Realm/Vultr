package voice.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "listening_session")
public data class ListeningSession(
  @PrimaryKey
  val id: Id,
  val bookId: BookId,
  val chapterId: ChapterId,
  @ColumnInfo(defaultValue = "PAUSED")
  val action: String,
  @ColumnInfo(defaultValue = "0")
  val positionInChapter: Long,
  @ColumnInfo(defaultValue = "1970-01-01T00:00:00Z")
  val createdAt: Instant,
  val listenedMs: Long,
) {

  public data class Id(val value: UUID) {
    public companion object {
      public fun random(): Id = Id(UUID.randomUUID())
    }
  }
}

public enum class ListeningHistoryAction {
  Played,
  Paused,
  Jumped,
  SkippedToChapter,
  NewChapter,
  SleepTimer,
}
