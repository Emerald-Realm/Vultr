package voice.core.data.repo

import voice.core.data.BookId
import voice.core.data.ListeningSession

public interface ListeningSessionRepo {
  public suspend fun add(session: ListeningSession)
  public suspend fun forBook(bookId: BookId): List<ListeningSession>
  public suspend fun all(): List<ListeningSession>
  public suspend fun delete(id: ListeningSession.Id)
}
