package voice.features.bookmark

import voice.core.data.ListeningSession

data class HistoryItemViewState(
  val id: ListeningSession.Id,
  val chapterName: String,
  val timeWithinChapter: String,
  val action: String,
  val globalBookDuration: String,
)

data class HistoryDayGroup(
  val label: String,
  val summary: String,
  val items: List<HistoryItemViewState>,
)

data class BookmarkViewState(
  val groups: List<HistoryDayGroup>,
  val dialogViewState: BookmarkDialogViewState,
)

sealed interface BookmarkDialogViewState {
  data object None : BookmarkDialogViewState
  data object AddBookmark : BookmarkDialogViewState
}
