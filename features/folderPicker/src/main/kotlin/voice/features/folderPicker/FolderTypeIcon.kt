package voice.features.folderPicker

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import voice.core.data.folders.FolderType
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun FolderTypeIcon(folderType: FolderType) {
  Icon(
    painter = painterResource(folderType.iconRes()),
    contentDescription = folderType.contentDescription(),
  )
}

@DrawableRes
private fun FolderType.iconRes(): Int = when (this) {
  FolderType.SingleFile -> UiR.drawable.ic_mage_book
  FolderType.SingleFolder -> UiR.drawable.ic_mage_book
  FolderType.Root -> UiR.drawable.ic_mage_book
  FolderType.Author -> UiR.drawable.ic_mage_user_circle
}

@Composable
private fun FolderType.contentDescription(): String {
  val res = when (this) {
    FolderType.SingleFile,
    FolderType.SingleFolder,
    -> StringsR.string.folder_mode_single
    FolderType.Root -> StringsR.string.folder_mode_root
    FolderType.Author -> StringsR.string.folder_mode_author
  }
  return stringResource(res)
}
