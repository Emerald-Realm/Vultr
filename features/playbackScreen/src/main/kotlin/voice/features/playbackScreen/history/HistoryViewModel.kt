package voice.features.playbackScreen.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.BookId
import voice.core.data.ListeningHistoryAction
import voice.core.data.ListeningSession
import voice.core.data.positionInfo
import voice.core.data.repo.BookRepository
import voice.core.data.repo.ListeningSessionRepo
import voice.core.ui.formatTime
import voice.navigation.Navigator
import java.time.LocalDate
import java.time.ZoneId

@AssistedInject
class HistoryViewModel(
  private val listeningSessionRepo: ListeningSessionRepo,
  private val bookRepository: BookRepository,
  private val navigator: Navigator,
  dispatcherProvider: DispatcherProvider,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope(dispatcherProvider)
  private val _state = mutableStateOf(HistoryViewState(title = "", days = emptyList()))
  val state: State<HistoryViewState> get() = _state

  init {
    refresh()
  }

  private fun refresh() {
    scope.launch {
      val book = bookRepository.get(bookId)
      val chaptersById = book?.chapters?.associateBy { it.id }.orEmpty()
      val zone = ZoneId.systemDefault()
      val days = listeningSessionRepo.forBook(bookId)
        .sortedByDescending { it.createdAt }
        .groupBy { it.createdAt.atZone(zone).toLocalDate() }
        .map { (date, sessions) ->
          val totalListenedMs = sessions.sumOf { it.listenedMs }
          val totalMinutes = totalListenedMs / 60_000L
          val hours = totalMinutes / 60L
          val minutes = totalMinutes % 60L
          val summary = if (hours > 0) {
            "$hours hours listened"
          } else {
            "$minutes minutes listened"
          }
          HistoryDayViewState(
            date = date,
            summary = summary,
            entries = sessions.map { session ->
              val time = session.createdAt.atZone(zone).toLocalTime()
              val info = chaptersById[session.chapterId]?.positionInfo(session.positionInChapter)
              val positionText = if (info != null) {
                "${formatTime(info.positionInMarkMs, info.markDurationMs)}/${formatTime(info.markDurationMs, info.markDurationMs)}"
              } else {
                formatTime(session.positionInChapter)
              }
              HistoryEntryViewState(
                id = session.id,
                action = runCatching { ListeningHistoryAction.valueOf(session.action) }.getOrNull(),
                chapterName = info?.name,
                positionText = positionText,
                timeText = "%02d:%02d".format(time.hour, time.minute),
              )
            },
          )
        }
      _state.value = HistoryViewState(
        title = book?.content?.name.orEmpty(),
        days = days,
      )
    }
  }

  @Composable
  fun viewState(): HistoryViewState = state.value

  fun onCloseClick() {
    navigator.goBack()
  }

  fun onDelete(id: ListeningSession.Id) {
    scope.launch {
      listeningSessionRepo.delete(id)
      refresh()
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): HistoryViewModel
  }
}

data class HistoryViewState(
  val title: String,
  val days: List<HistoryDayViewState>,
)

data class HistoryDayViewState(
  val date: LocalDate,
  val summary: String,
  val entries: List<HistoryEntryViewState>,
)

data class HistoryEntryViewState(
  val id: ListeningSession.Id,
  val action: ListeningHistoryAction?,
  val chapterName: String?,
  val positionText: String,
  val timeText: String,
)
