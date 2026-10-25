package voice.features.review

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import voice.core.data.RatingReminderState
import voice.core.data.listening.ListeningStats
import voice.core.data.listening.RATING_WINDOW_FIRST_DAY
import voice.core.data.listening.RATING_WINDOW_LAST_DAY
import voice.core.data.listening.RATING_WINDOW_LISTENED_MS
import voice.core.data.listening.listenedMsInRatingWindow
import voice.core.data.store.RatingReminderStateStore
import voice.core.featureflag.FeatureFlag
import voice.core.featureflag.RatingReminderEnabledFeatureFlagQualifier
import java.time.LocalDate

@SingleIn(AppScope::class)
@Inject
class RatingReminderCoordinator(
  @RatingReminderStateStore private val stateStore: DataStore<RatingReminderState>,
  @RatingReminderEnabledFeatureFlagQualifier private val enabledFeatureFlag: FeatureFlag<Boolean>,
) {
  private val mutex = Mutex()

  /**
   * Claims the current month before the dialog is shown, so dismissing it in any way cannot make it
   * appear again during that month.
   */
  suspend fun tryPresent(
    today: LocalDate,
    stats: ListeningStats,
  ): Boolean {
    if (!enabledFeatureFlag.get()) return false
    return mutex.withLock {
      var claimed = false
      stateStore.updateData { state ->
        val eligible =
          isRatingReminderEligible(
            today = today,
            listenedThisWindowMs = stats.listenedMsInRatingWindow(today.year, today.monthValue),
            lastPresentedMonth = state.lastPresentedMonth,
            selfDeclaredRated = state.selfDeclaredRated,
            enabled = enabledFeatureFlag.get(),
          )
        if (eligible) {
          claimed = true
          state.copy(lastPresentedMonth = today.monthKey())
        } else {
          state
        }
      }
      claimed
    }
  }

  suspend fun isSelfDeclaredRated(): Boolean = stateStore.data.first().selfDeclaredRated

  suspend fun markAlreadyRated() {
    mutex.withLock {
      stateStore.updateData { it.copy(selfDeclaredRated = true) }
    }
  }
}

internal fun isRatingReminderEligible(
  today: LocalDate,
  listenedThisWindowMs: Long,
  lastPresentedMonth: String?,
  selfDeclaredRated: Boolean,
  enabled: Boolean,
): Boolean {
  if (!enabled || selfDeclaredRated) return false
  if (today.dayOfMonth !in RATING_WINDOW_FIRST_DAY..RATING_WINDOW_LAST_DAY) return false
  if (listenedThisWindowMs < RATING_WINDOW_LISTENED_MS) return false
  return lastPresentedMonth != today.monthKey()
}

internal fun LocalDate.monthKey(): String = "$year-${monthValue.toString().padStart(2, '0')}"
