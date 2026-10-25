package voice.features.review

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import voice.core.data.RatingReminderState
import voice.core.data.listening.ListeningStats
import voice.core.data.listening.listenedMsInRatingWindow
import voice.core.featureflag.MemoryFeatureFlag
import java.time.LocalDate

class RatingReminderEligibilityTest {
  @Test
  fun `only local dates eight through fourteen are eligible`() {
    assertFalse(eligible(LocalDate.of(2026, 10, 7)))
    assertTrue(eligible(LocalDate.of(2026, 10, 8)))
    assertTrue(eligible(LocalDate.of(2026, 10, 14)))
    assertFalse(eligible(LocalDate.of(2026, 10, 15)))
  }

  @Test
  fun `one hour is required to the millisecond`() {
    assertFalse(eligible(LocalDate.of(2026, 10, 10), listenedMs = 3_599_999L))
    assertTrue(eligible(LocalDate.of(2026, 10, 10), listenedMs = 3_600_000L))
  }

  @Test
  fun `the reminder is presented once per calendar month`() {
    assertFalse(eligible(LocalDate.of(2026, 10, 10), lastPresentedMonth = "2026-10"))
    assertTrue(eligible(LocalDate.of(2026, 10, 10), lastPresentedMonth = "2026-09"))
    assertTrue(eligible(LocalDate.of(2027, 1, 10), lastPresentedMonth = "2026-12"))
  }

  @Test
  fun `a self declared rating stops all reminders`() {
    assertFalse(eligible(LocalDate.of(2026, 10, 10), selfDeclaredRated = true))
  }

  @Test
  fun `a disabled feature never presents`() {
    assertFalse(eligible(LocalDate.of(2026, 10, 10), enabled = false))
  }

  @Test
  fun `listening is summed only from the current local window`() {
    val stats =
      ListeningStats(
        dailyListenedMs =
          mapOf(
            "2026-09-30" to 3_000_000L,
            "2026-10-07" to 3_000_000L,
            "2026-10-08" to 1_800_000L,
            "2026-10-14" to 1_800_000L,
            "2026-10-15" to 3_000_000L,
          ),
      )

    assertEquals(3_600_000L, stats.listenedMsInRatingWindowForTest())

    assertTrue(
      isRatingReminderEligible(
        today = LocalDate.of(2026, 10, 12),
        listenedThisWindowMs = stats.listenedMsInRatingWindowForTest(),
        lastPresentedMonth = null,
        selfDeclaredRated = false,
        enabled = true,
      ),
    )
  }

  private fun eligible(
    today: LocalDate,
    listenedMs: Long = 3_600_000L,
    lastPresentedMonth: String? = null,
    selfDeclaredRated: Boolean = false,
    enabled: Boolean = true,
  ): Boolean =
    isRatingReminderEligible(
      today = today,
      listenedThisWindowMs = listenedMs,
      lastPresentedMonth = lastPresentedMonth,
      selfDeclaredRated = selfDeclaredRated,
      enabled = enabled,
    )
}

class RatingReminderCoordinatorTest {
  private val store = MemoryDataStore(RatingReminderState())
  private val coordinator =
    RatingReminderCoordinator(
      stateStore = store,
      enabledFeatureFlag = MemoryFeatureFlag(true),
    )

  @Test
  fun `a month is claimed only once`() =
    runTest {
      val stats = ListeningStats(dailyListenedMs = mapOf("2026-10-10" to 3_600_000L))

      assertTrue(coordinator.tryPresent(LocalDate.of(2026, 10, 10), stats))
      assertFalse(coordinator.tryPresent(LocalDate.of(2026, 10, 11), stats))
      assertEquals("2026-10", store.data.first().lastPresentedMonth)
    }

  @Test
  fun `a declared rating suppresses future months`() =
    runTest {
      coordinator.markAlreadyRated()
      val stats = ListeningStats(dailyListenedMs = mapOf("2026-11-10" to 3_600_000L))

      assertFalse(coordinator.tryPresent(LocalDate.of(2026, 11, 10), stats))
      assertTrue(coordinator.isSelfDeclaredRated())
    }

  @Test
  fun `a missed window is not carried into the next month`() =
    runTest {
      val octoberStats = ListeningStats(dailyListenedMs = mapOf("2026-10-10" to 3_600_000L))

      assertFalse(coordinator.tryPresent(LocalDate.of(2026, 10, 20), octoberStats))
      assertFalse(coordinator.tryPresent(LocalDate.of(2026, 11, 3), octoberStats))
      assertTrue(
        coordinator.tryPresent(LocalDate.of(2026, 11, 8), ListeningStats(dailyListenedMs = mapOf("2026-11-08" to 3_600_000L))),
      )
    }
}

private fun ListeningStats.listenedMsInRatingWindowForTest(): Long = listenedMsInRatingWindow(year = 2026, month = 10)

private class MemoryDataStore<T>(
  initial: T,
) : DataStore<T> {
  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T = value.updateAndGet { transform(it) }
}
