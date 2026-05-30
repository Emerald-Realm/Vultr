package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import voice.core.data.BookId
import voice.core.data.ListeningSession
import voice.core.data.repo.internals.dao.ListeningSessionDao

@ContributesBinding(AppScope::class)
public class ListeningSessionRepoImpl
internal constructor(
  private val dao: ListeningSessionDao,
) : ListeningSessionRepo {

  override suspend fun add(session: ListeningSession) {
    dao.insert(session)
  }

  override suspend fun forBook(bookId: BookId): List<ListeningSession> = dao.forBook(bookId)

  override suspend fun all(): List<ListeningSession> = dao.all()

  override suspend fun delete(id: ListeningSession.Id) {
    dao.delete(id)
  }
}
