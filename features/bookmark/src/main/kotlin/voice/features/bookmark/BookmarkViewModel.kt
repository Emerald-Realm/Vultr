package voice.features.bookmark

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ListeningHistoryAction
import voice.core.data.ListeningSession
import voice.core.data.markForPosition
import voice.core.data.repo.BookRepository
import voice.core.data.repo.BookmarkRepo
import voice.core.data.repo.ListeningSessionRepo
import voice.core.data.store.CurrentBookStore
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlayStateManager
import voice.core.strings.R
import voice.core.ui.formatTime
import voice.navigation.Navigator
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AssistedInject
class BookmarkViewModel(
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val repo: BookRepository,
  private val bookmarkRepo: BookmarkRepo,
  private val sessionRepo: ListeningSessionRepo,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  private val navigator: Navigator,
  private val context: Context,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope()
  private var chapters by mutableStateOf<List<Chapter>>(emptyList())
  private var sessions by mutableStateOf<List<ListeningSession>>(emptyList())
  private var dialogViewState: BookmarkDialogViewState by mutableStateOf(BookmarkDialogViewState.None)

  @Composable
  fun viewState(): BookmarkViewState {
    LaunchedEffect(bookId) {
      val book = repo.get(bookId)
      if (book != null) {
        chapters = book.chapters
      }
      sessions = sessionRepo.forBook(bookId)
    }
    return BookmarkViewState(
      groups = buildGroups(),
      dialogViewState = dialogViewState,
    )
  }

  private fun buildGroups(): List<HistoryDayGroup> {
    if (sessions.isEmpty() || chapters.isEmpty()) return emptyList()
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val totalDurationMs = chapters.sumOf { it.duration }
    return sessions
      .groupBy { it.createdAt.atZone(zone).toLocalDate() }
      .toSortedMap(compareByDescending { it })
      .map { (date, daySessions) ->
        val label = when (date) {
          today -> context.getString(R.string.bookmark_today)
          yesterday -> context.getString(R.string.bookmark_yesterday)
          else -> dateFormatter.format(date)
        }
        HistoryDayGroup(
          label = label,
          summary = formatListened(daySessions.sumOf { it.listenedMs }),
          items = daySessions.map { it.toItemViewState(totalDurationMs) },
        )
      }
  }

  private fun ListeningSession.toItemViewState(totalDurationMs: Long): HistoryItemViewState {
    val chapter = chapters.singleOrNull { it.id == chapterId }
    val chapterName = chapter?.markForPosition(positionInChapter)?.name
      ?: chapter?.name
      ?: ""
    val priorChaptersDuration = chapters.takeWhile { it.id != chapterId }.sumOf { it.duration }
    val globalDurationMs = priorChaptersDuration + positionInChapter
    val chapterDuration = chapter?.duration ?: 0L
    return HistoryItemViewState(
      id = id,
      chapterName = chapterName,
      timeWithinChapter = "${formatTime(positionInChapter, chapterDuration)} / ${formatTime(chapterDuration, chapterDuration)}",
      action = actionLabel(action),
      globalBookDuration = formatTime(globalDurationMs, totalDurationMs),
    )
  }

  private fun actionLabel(action: String): String {
    val resId = when (runCatching { ListeningHistoryAction.valueOf(action) }.getOrNull()) {
      ListeningHistoryAction.Played -> R.string.history_action_played
      ListeningHistoryAction.Paused -> R.string.history_action_paused
      ListeningHistoryAction.Jumped -> R.string.history_action_jumped
      ListeningHistoryAction.SkippedToChapter -> R.string.history_action_skipped_to_chapter
      ListeningHistoryAction.NewChapter -> R.string.history_action_new_chapter
      ListeningHistoryAction.SleepTimer -> R.string.history_action_sleep_timer
      null -> R.string.history_action_paused
    }
    return context.getString(resId)
  }

  private fun formatListened(ms: Long): String {
    val totalMinutes = ms / 60_000L
    val hours = (totalMinutes / 60L).toInt()
    val minutes = (totalMinutes % 60L).toInt()
    return when {
      hours > 0 -> context.resources.getQuantityString(R.plurals.hours_listened, hours, hours)
      else -> context.resources.getQuantityString(R.plurals.minutes_listened, minutes, minutes)
    }
  }

  fun deleteEntry(id: ListeningSession.Id) {
    scope.launch {
      sessionRepo.delete(id)
      sessions = sessions.filter { it.id != id }
    }
  }

  fun selectEntry(id: ListeningSession.Id) {
    val session = sessions.find { it.id == id } ?: return
    val wasPlaying = playStateManager.playState == PlayStateManager.PlayState.Playing
    scope.launch {
      currentBookStore.updateData { bookId }
    }
    playerController.setPosition(session.positionInChapter, session.chapterId)
    if (wasPlaying) {
      playerController.play()
    }
    navigator.goBack()
  }

  fun addBookmark(name: String) {
    scope.launch {
      val book = repo.get(bookId) ?: return@launch
      bookmarkRepo.addBookmarkAtBookPosition(
        book = book,
        title = name,
        setBySleepTimer = false,
      )
    }
  }

  fun closeDialog() {
    dialogViewState = BookmarkDialogViewState.None
  }

  fun onAddClick() {
    dialogViewState = BookmarkDialogViewState.AddBookmark
  }

  fun closeScreen() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): BookmarkViewModel
  }
}
