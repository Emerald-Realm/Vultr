package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.BookId
import voice.core.data.ListeningSession

@Dao
public interface ListeningSessionDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(session: ListeningSession)

  @Query("SELECT * FROM listening_session WHERE bookId = :bookId ORDER BY createdAt DESC")
  public suspend fun forBook(bookId: BookId): List<ListeningSession>

  @Query("SELECT * FROM listening_session ORDER BY createdAt DESC")
  public suspend fun all(): List<ListeningSession>

  @Query("DELETE FROM listening_session WHERE id = :id")
  public suspend fun delete(id: ListeningSession.Id)
}
