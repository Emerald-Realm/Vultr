package voice.core.playback.playstate

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import voice.core.data.BookId
import voice.core.data.ChapterId
import voice.core.data.listening.ListeningStats
import voice.core.data.listening.PlayedRange
import voice.core.data.repo.BookRepository
import voice.core.data.repo.ListeningStatsRepo
import voice.core.playback.session.MediaId
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListeningTrackerTest {
  private val bookId = BookId("book")
  private val chapterId = ChapterId("chapter")
  private val statsRepo = MemoryListeningStatsRepo()
  private val bookRepo = mockk<BookRepository>()

  init {
    coEvery { bookRepo.get(any()) } returns null
  }

  @Test
  fun `playback speed changes coverage but not elapsed listening time`() = runTest {
    val playback = TestPlayback(speed = 2f)
    val tracker = tracker(playback.player)

    tracker.onIsPlayingChanged(true)
    advance(30_000L)
    playback.positionMs = 60_000L
    tracker.onIsPlayingChanged(false)
    runCurrent()

    val stats = statsRepo.value
    assertEquals(30_000L, stats.lifetimeListenedMs)
    assertEquals(listOf(PlayedRange(0L, 60_000L)), stats.playedRanges[bookId.value]?.get(chapterId.value))
  }

  @Test
  fun `paused time is not credited`() = runTest {
    val playback = TestPlayback()
    val tracker = tracker(playback.player)

    tracker.onIsPlayingChanged(true)
    advance(5_000L)
    playback.positionMs = 5_000L
    tracker.onIsPlayingChanged(false)
    runCurrent()

    advance(60_000L)
    playback.positionMs = 65_000L
    tracker.onIsPlayingChanged(false)
    runCurrent()

    assertEquals(5_000L, statsRepo.value.lifetimeListenedMs)
  }

  @Test
  fun `a seek gap is not added to played coverage`() = runTest {
    val playback = TestPlayback()
    val tracker = tracker(playback.player)

    tracker.onIsPlayingChanged(true)
    advance(10_000L)
    playback.positionMs = 100_000L
    tracker.onPositionDiscontinuity(positionInfo(100_000L), positionInfo(0L), Player.DISCONTINUITY_REASON_SEEK)
    advance(10_000L)
    playback.positionMs = 110_000L
    tracker.onIsPlayingChanged(false)
    runCurrent()

    val ranges = statsRepo.value.playedRanges[bookId.value]?.get(chapterId.value).orEmpty()
    assertEquals(listOf(PlayedRange(100_000L, 110_000L)), ranges)
    assertEquals(20_000L, statsRepo.value.lifetimeListenedMs)
  }

  @Test
  fun `releasing the tracker stops future measurement`() = runTest {
    val playback = TestPlayback()
    val tracker = tracker(playback.player)

    tracker.release()
    advance(10_000L)
    playback.positionMs = 10_000L
    tracker.onIsPlayingChanged(false)
    runCurrent()

    assertEquals(0L, statsRepo.value.lifetimeListenedMs)
  }

  private fun TestScope.tracker(player: Player): ListeningTracker {
    val tracker = ListeningTracker(statsRepo = statsRepo, bookRepo = bookRepo, scope = backgroundScope)
    tracker.attachTo(player)
    return tracker
  }

  private fun advance(durationMs: Long) {
    ShadowSystemClock.advanceBy(Duration.ofMillis(durationMs))
  }

  private fun positionInfo(positionMs: Long): Player.PositionInfo =
    Player.PositionInfo(
      null,
      0,
      null,
      null,
      0,
      positionMs,
      positionMs,
      C.INDEX_UNSET,
      C.INDEX_UNSET,
    )

  private inner class TestPlayback(speed: Float = 1f) {
    private val mediaItem =
      MediaItem.Builder()
        .setMediaId(
          Json.encodeToString(
            MediaId.serializer(),
            MediaId.Chapter(bookId = bookId, chapterId = chapterId),
          ),
        )
        .build()

    var positionMs: Long = 0L

    val player: Player =
      mockk(relaxed = true) {
        every { currentMediaItem } returns mediaItem
        every { currentPosition } answers { positionMs }
        every { playbackParameters } returns PlaybackParameters(speed)
        every { isPlaying } returns true
      }
  }
}

private class MemoryListeningStatsRepo : ListeningStatsRepo {
  private val state = MutableStateFlow(ListeningStats())

  val value: ListeningStats get() = state.value

  override val stats: Flow<ListeningStats> = state

  override suspend fun update(transform: (ListeningStats) -> ListeningStats): ListeningStats = state.updateAndGet(transform)
}
