package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.listening.ListeningStats

public interface ListeningStatsRepo {
  public val stats: Flow<ListeningStats>

  public suspend fun update(transform: (ListeningStats) -> ListeningStats): ListeningStats
}
