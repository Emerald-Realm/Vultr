package voice.features.bookOverview.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun BookFolderIcon(
  withHint: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    IconButton(onClick = onClick) {
      Icon(
        painter = painterResource(UiR.drawable.ic_mage_folder),
        contentDescription = stringResource(StringsR.string.audiobook_folders_title),
      )
    }
    if (withHint) {
      AddBookHint()
    }
  }
}
