package voice.features.bookOverview.bottomSheet

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import voice.core.data.BookId
import voice.features.bookOverview.di.BookOverviewScope
import voice.navigation.Destination
import voice.navigation.Navigator

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookMenuViewModel(private val navigator: Navigator) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> = listOf(BottomSheetItem.EditBook)

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    if (item == BottomSheetItem.EditBook) {
      navigator.goTo(Destination.EditBook(bookId))
    }
  }
}
