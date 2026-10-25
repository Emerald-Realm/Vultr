package voice.core.data.repo

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import voice.core.data.listening.ListeningStats
import voice.core.data.repo.internals.dao.ListeningSessionDao
import voice.core.data.store.ListeningStatsStore

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class ListeningStatsRepoImpl
  internal constructor(
    @ListeningStatsStore private val store: DataStore<ListeningStats>,
    private val listeningSessionDao: ListeningSessionDao,
  ) : ListeningStatsRepo {
    private val seedMutex = Mutex()

    override val stats: Flow<ListeningStats> =
      store.data
        .onStart { seedLegacyListeningTime() }

    override suspend fun update(transform: (ListeningStats) -> ListeningStats): ListeningStats = store.updateData(transform)

    /**
     * Older versions only recorded wall-clock session durations. Those sessions cannot provide
     * played coverage or reliable date attribution, so they only seed the lifetime counter once.
     */
    private suspend fun seedLegacyListeningTime() {
      seedMutex.withLock {
        if (store.data.first().historySeeded) return
        val legacyListenedMs = listeningSessionDao.all().sumOf { it.listenedMs.coerceAtLeast(0L) }
        store.updateData { current ->
          if (current.historySeeded) {
            current
          } else {
            current.copy(
              lifetimeListenedMs = current.lifetimeListenedMs + legacyListenedMs,
              historySeeded = true,
            )
          }
        }
      }
    }
  }
