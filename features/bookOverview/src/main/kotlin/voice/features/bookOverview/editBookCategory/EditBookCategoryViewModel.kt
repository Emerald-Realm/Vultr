package voice.features.bookOverview.editBookCategory

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.bottomSheet.BottomSheetItemViewModel
import voice.features.bookOverview.di.BookOverviewScope

@SingleIn(BookOverviewScope::class)
@ContributesIntoSet(BookOverviewScope::class)
class EditBookCategoryViewModel(private val repo: BookRepository) : BottomSheetItemViewModel {

  override suspend fun items(bookId: BookId): List<BottomSheetItem> {
    if (repo.get(bookId) == null) return emptyList()
    // Design shows both regardless of current state.
    return listOf(
      BottomSheetItem.BookCategoryMarkAsNotStarted,
      BottomSheetItem.BookCategoryMarkAsCompleted,
    )
  }

  override suspend fun onItemClick(
    bookId: BookId,
    item: BottomSheetItem,
  ) {
    val book = repo.get(bookId) ?: return

    val (currentChapter, positionInChapter) = when (item) {
      BottomSheetItem.BookCategoryMarkAsNotStarted -> {
        book.chapters.first().id to 0L
      }
      BottomSheetItem.BookCategoryMarkAsCompleted -> {
        val lastChapter = book.chapters.last()
        lastChapter.id to lastChapter.duration
      }
      else -> return
    }

    repo.updateBook(book.id) {
      it.copy(
        currentChapter = currentChapter,
        positionInChapter = positionInChapter,
      )
    }
  }
}
