package voice.core.playback.playstate

import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import voice.core.data.Book
import voice.core.data.BookId
import voice.core.data.ChapterId
import voice.core.data.listening.ChapterDuration
import voice.core.data.listening.PlayedRange
import voice.core.data.listening.addListened
import voice.core.data.listening.addPlayedRange
import voice.core.data.listening.attributeListening
import voice.core.data.listening.awardBookIfComplete
import voice.core.data.listening.merge
import voice.core.data.repo.BookRepository
import voice.core.data.repo.ListeningStatsRepo
import voice.core.logging.api.Logger
import voice.core.playback.di.PlaybackScope
import voice.core.playback.session.MediaId
import voice.core.playback.session.toMediaIdOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Measures genuine active playback with a monotonic clock and records played media coverage.
 *
 * Only the time between an isPlaying transition and its opposite is credited, so buffering, pauses,
 * seeking and idle time are excluded while background playback still counts. Media coverage is kept
 * separately because playback speed changes media position and elapsed time differently.
 */
@SingleIn(PlaybackScope::class)
@Inject
class ListeningTracker(
  private val statsRepo: ListeningStatsRepo,
  private val bookRepo: BookRepository,
  private val scope: CoroutineScope,
) : Player.Listener {
  private var player: Player? = null
  private var tickJob: Job? = null
  private val writeMutex = Mutex()

  private var segment: Segment? = null
  private var lastCheckpointMs = 0L

  private var pendingListenedMs = 0L
  private val pendingByDay = mutableMapOf<String, Long>()
  private val pendingRanges = mutableMapOf<RangeKey, List<PlayedRange>>()
  private val chapterDurations = mutableMapOf<String, List<ChapterDuration>>()

  fun attachTo(player: Player) {
    this.player = player
    player.addListener(this)
    tickJob =
      scope.launch {
        while (isActive) {
          delay(TICK_INTERVAL_MS)
          tick()
        }
      }
  }

  override fun onIsPlayingChanged(isPlaying: Boolean) {
    if (isPlaying) {
      startSegment()
    } else {
      endSegment()
    }
  }

  override fun onPositionDiscontinuity(
    oldPosition: Player.PositionInfo,
    newPosition: Player.PositionInfo,
    reason: Int,
  ) {
    val current = segment ?: return
    accrue(current, SystemClock.elapsedRealtime(), Clock.System.now(), current.lastPositionMs)
    current.lastPositionMs = playerPositionFor(current) ?: current.lastPositionMs
    current.lastElapsedRealtimeMs = SystemClock.elapsedRealtime()
    current.lastWallClock = Clock.System.now()
  }

  override fun onMediaItemTransition(
    mediaItem: androidx.media3.common.MediaItem?,
    reason: Int,
  ) {
    endSegment()
    if (player?.isPlaying == true) startSegment()
  }

  override fun onPlaybackStateChanged(playbackState: Int) {
    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
      endSegment()
    }
  }

  override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
    segment?.speed = playbackParameters.speed
  }

  suspend fun flushNow() {
    val snapshot = takePending() ?: return
    write(snapshot)
  }

  fun release() {
    tickJob?.cancel()
    player?.removeListener(this)
    player = null
    segment = null
  }

  private fun tick() {
    val current =
      segment ?: run {
        if (player?.isPlaying == true) startSegment()
        return
      }
    val player = player ?: return
    if (!player.isPlaying) {
      endSegment()
      return
    }
    val nowMs = SystemClock.elapsedRealtime()
    accrue(current, nowMs, Clock.System.now(), playerPositionFor(current))
    if (nowMs - lastCheckpointMs >= CHECKPOINT_INTERVAL_MS) {
      lastCheckpointMs = nowMs
      flush()
    }
  }

  private fun startSegment() {
    if (segment != null) return
    val player = player ?: return
    val mediaId = player.currentMediaItem?.mediaId?.toMediaIdOrNull() as? MediaId.Chapter ?: return
    val nowMs = SystemClock.elapsedRealtime()
    segment =
      Segment(
        bookId = mediaId.bookId,
        chapterId = mediaId.chapterId,
        lastPositionMs = player.currentPosition.coerceAtLeast(0L),
        lastElapsedRealtimeMs = nowMs,
        lastWallClock = Clock.System.now(),
        speed = player.playbackParameters.speed,
      )
    lastCheckpointMs = nowMs
  }

  private fun endSegment() {
    val current = segment ?: return
    val now = SystemClock.elapsedRealtime()
    accrue(current, now, Clock.System.now(), playerPositionFor(current))
    segment = null
    flush()
  }

  private fun accrue(
    segment: Segment,
    nowElapsedRealtimeMs: Long,
    nowWallClock: Instant,
    positionMs: Long?,
  ) {
    val elapsedMs = nowElapsedRealtimeMs - segment.lastElapsedRealtimeMs
    if (elapsedMs <= 0L) {
      segment.lastElapsedRealtimeMs = nowElapsedRealtimeMs
      segment.lastWallClock = nowWallClock
      return
    }

    val creditedMs = elapsedMs.coerceAtMost(MAX_SINGLE_ACCRUAL_MS)
    val startWallClock =
      if (creditedMs < elapsedMs) {
        nowWallClock - creditedMs.milliseconds
      } else {
        segment.lastWallClock
      }
    attributeListening(
      elapsedMs = creditedMs,
      startedAt = startWallClock,
      endedAt = nowWallClock,
      timeZone = TimeZone.currentSystemDefault(),
    ).forEach { (date, listenedMs) ->
      pendingByDay[date.toString()] = (pendingByDay[date.toString()] ?: 0L) + listenedMs
    }
    pendingListenedMs += creditedMs

    if (positionMs != null && positionMs >= segment.lastPositionMs) {
      val maxExpectedAdvance = (elapsedMs * segment.speed * MEDIA_ADVANCE_TOLERANCE).toLong() + MEDIA_ADVANCE_SLACK_MS
      val advance = positionMs - segment.lastPositionMs
      if (advance in 0..maxExpectedAdvance) {
        addPendingRange(segment.bookId, segment.chapterId, segment.lastPositionMs, positionMs)
      }
    }
    positionMs?.let { segment.lastPositionMs = it }
    segment.lastElapsedRealtimeMs = nowElapsedRealtimeMs
    segment.lastWallClock = nowWallClock
  }

  private fun addPendingRange(
    bookId: BookId,
    chapterId: ChapterId,
    startMs: Long,
    endMs: Long,
  ) {
    if (endMs <= startMs) return
    val key = RangeKey(bookId, chapterId)
    pendingRanges[key] = pendingRanges[key].orEmpty().merge(PlayedRange(startMs, endMs))
  }

  private fun playerPositionFor(segment: Segment): Long? {
    val mediaId = player?.currentMediaItem?.mediaId?.toMediaIdOrNull() as? MediaId.Chapter ?: return null
    if (mediaId.bookId != segment.bookId || mediaId.chapterId != segment.chapterId) return null
    val position = player?.currentPosition ?: return null
    return position.takeIf { it >= 0L && it != C.TIME_UNSET }
  }

  private fun takePending(): Pending? {
    if (pendingListenedMs <= 0L && pendingRanges.isEmpty()) return null
    val snapshot =
      Pending(
        listenedMs = pendingListenedMs,
        byDay = pendingByDay.toMap(),
        ranges =
          pendingRanges.map { (key, ranges) ->
            PendingRange(key.bookId, key.chapterId, ranges.toList())
          },
      )
    pendingListenedMs = 0L
    pendingByDay.clear()
    pendingRanges.clear()
    return snapshot
  }

  private fun flush() {
    val snapshot = takePending() ?: return
    scope.launch { write(snapshot) }
  }

  private suspend fun write(snapshot: Pending) {
    writeMutex.withLock {
      val books =
        snapshot.ranges
          .map { it.bookId }
          .distinct()
          .mapNotNull { bookId -> runCatching { bookRepo.get(bookId) }.getOrNull() }
      val durations = books.associate { book -> book.id.value to book.chapterDurations() }
      try {
        statsRepo.update { current ->
          var updated = current.addListened(snapshot.listenedMs, snapshot.byDay)
          snapshot.ranges.forEach { pending ->
            pending.ranges.forEach { range ->
              updated =
                updated.addPlayedRange(
                  bookId = pending.bookId,
                  chapterId = pending.chapterId,
                  startMs = range.startMs,
                  endMs = range.endMs,
                )
            }
          }
          durations.forEach { (bookId, chapters) ->
            updated = updated.awardBookIfComplete(BookId(bookId), chapters)
          }
          updated
        }
      } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
      } catch (e: Exception) {
        Logger.w(e, "Failed to persist listening stats")
      }
    }
  }

  private fun Book.chapterDurations(): List<ChapterDuration> =
    chapterDurations.getOrPut(id.value) {
      chapters.map { ChapterDuration(it.id, it.duration) }
    }

  private data class Segment(
    val bookId: BookId,
    val chapterId: ChapterId,
    var lastPositionMs: Long,
    var lastElapsedRealtimeMs: Long,
    var lastWallClock: Instant,
    var speed: Float,
  )

  private data class RangeKey(
    val bookId: BookId,
    val chapterId: ChapterId,
  )

  private data class Pending(
    val listenedMs: Long,
    val byDay: Map<String, Long>,
    val ranges: List<PendingRange>,
  )

  private data class PendingRange(
    val bookId: BookId,
    val chapterId: ChapterId,
    val ranges: List<PlayedRange>,
  )
}

private const val TICK_INTERVAL_MS = 1_000L
private const val CHECKPOINT_INTERVAL_MS = 15_000L
private const val MAX_SINGLE_ACCRUAL_MS = 6 * 60 * 60 * 1_000L
private const val MEDIA_ADVANCE_TOLERANCE = 1.5f
private const val MEDIA_ADVANCE_SLACK_MS = 3_000L
