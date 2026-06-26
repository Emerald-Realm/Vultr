package voice.features.bookOverview.bottomSheet

import androidx.annotation.DrawableRes
import voice.core.ui.R as UiR

internal data class EditBookBottomSheetState(val items: List<BottomSheetItem>)

// Library long-press / row menu. Enum order is the display order (BottomSheetViewModel sorts by ordinal).
enum class BottomSheetItem(
  val title: String,
  @DrawableRes val iconRes: Int,
  val destructive: Boolean = false,
) {
  BookCategoryMarkAsNotStarted("Mark as Not Started", UiR.drawable.ic_mage_book),
  BookCategoryMarkAsCompleted("Mark as Completed", UiR.drawable.ic_mage_check),
  EditBook("Edit Book", UiR.drawable.ic_mage_edit),
  DeleteBook("Delete Book", UiR.drawable.ic_mage_trash, destructive = true),
}
