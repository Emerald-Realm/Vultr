package voice.features.bookOverview.bottomSheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

internal data class EditBookBottomSheetState(val items: List<BottomSheetItem>)

enum class BottomSheetItem(
  @StringRes val titleRes: Int,
  @DrawableRes val iconRes: Int,
) {
  Title(StringsR.string.change_book_name, UiR.drawable.ic_mage_edit),
  InternetCover(StringsR.string.download_book_cover, UiR.drawable.ic_mage_download),
  FileCover(StringsR.string.pick_book_cover, UiR.drawable.ic_mage_image),
  DeleteBook(StringsR.string.delete_book_bottom_sheet_title, UiR.drawable.ic_mage_trash),
  BookCategoryMarkAsNotStarted(StringsR.string.mark_as_not_started, UiR.drawable.ic_mage_hour_glass),
  BookCategoryMarkAsCurrent(StringsR.string.mark_as_current, UiR.drawable.ic_mage_play),
  BookCategoryMarkAsCompleted(StringsR.string.mark_as_completed, UiR.drawable.ic_mage_check),
}
