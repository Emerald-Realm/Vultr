package voice.features.bookOverview.fileCover

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import voice.core.data.BookId
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookOverviewScope

// Cover changes now live inside the Edit Book screen, not the library menu.
@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class FileCoverViewModel : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> = emptyList()

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
  }
}
