package voice.core.playback.playstate

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.ListeningHistoryAction
import voice.core.data.ListeningSession
import voice.core.data.repo.BookRepository
import voice.core.data.repo.ListeningSessionRepo
import voice.core.data.store.CurrentBookStore
import voice.core.logging.api.Logger
import java.time.Instant

@Inject
@SingleIn(AppScope::class)
class PlaybackHistoryRecorder(
  private val repo: BookRepository,
  private val sessionRepo: ListeningSessionRepo,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val playStateManager: PlayStateManager,
) {

  private val scope = MainScope()
  private var playingSince: Instant? = null
  private var suppressNextPause = false

  fun onPlay() {
    playingSince = Instant.now()
    record(ListeningHistoryAction.Played, listenedMs = 0)
  }

  fun onPause() {
    if (suppressNextPause) {
      suppressNextPause = false
      return
    }
    record(ListeningHistoryAction.Paused, listenedMs = consumeListened(keepPlaying = false))
  }

  fun onSleepEnded() {
    record(ListeningHistoryAction.SleepTimer, listenedMs = consumeListened(keepPlaying = false))
    suppressNextPause = true
  }

  fun onJump() {
    record(ListeningHistoryAction.Jumped, listenedMs = consumeListened(keepPlaying = true))
  }

  fun onNewChapter() {
    record(ListeningHistoryAction.NewChapter, listenedMs = consumeListened(keepPlaying = true))
  }

  fun onSkippedToChapter() {
    record(ListeningHistoryAction.SkippedToChapter, listenedMs = consumeListened(keepPlaying = true))
  }

  private fun consumeListened(keepPlaying: Boolean): Long {
    val since = playingSince ?: return 0
    val now = Instant.now()
    val elapsed = (now.toEpochMilli() - since.toEpochMilli()).coerceAtLeast(0)
    playingSince = if (keepPlaying && playStateManager.playState == PlayStateManager.PlayState.Playing) {
      now
    } else {
      null
    }
    return elapsed
  }

  private fun record(
    action: ListeningHistoryAction,
    listenedMs: Long,
  ) {
    scope.launch {
      val bookId = currentBookStore.data.first() ?: return@launch
      val book = repo.get(bookId) ?: return@launch
      runCatching {
        sessionRepo.add(
          ListeningSession(
            id = ListeningSession.Id.random(),
            bookId = bookId,
            chapterId = book.content.currentChapter,
            action = action.name,
            positionInChapter = book.content.positionInChapter,
            createdAt = Instant.now(),
            listenedMs = listenedMs,
          ),
        )
      }.onFailure { Logger.w(it, "Failed to record playback history") }
    }
  }
}
