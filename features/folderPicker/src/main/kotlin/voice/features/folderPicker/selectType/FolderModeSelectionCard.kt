package voice.features.folderPicker.selectType

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.core.ui.VoiceTheme
import voice.core.ui.R as UiR
import voice.core.strings.R as StringsR

@Composable
internal fun FolderModeSelectionCard(
  onFolderModeSelect: (FolderMode) -> Unit,
  selectedFolderMode: FolderMode,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FolderMode.entries.forEach { folderMode ->
      FolderModeOption(
        folderMode = folderMode,
        selected = selectedFolderMode == folderMode,
        onClick = { onFolderModeSelect(folderMode) },
      )
    }
  }
}

@Composable
private fun FolderModeOption(
  folderMode: FolderMode,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(999.dp)
  val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
  val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
      .border(1.dp, borderColor, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(
      modifier = Modifier.size(22.dp),
      painter = painterResource(id = folderMode.icon()),
      contentDescription = null,
      tint = contentColor,
    )
    Text(
      text = stringResource(id = folderMode.title()),
      style = MaterialTheme.typography.bodyMedium,
      color = contentColor,
    )
  }
}

@DrawableRes
private fun FolderMode.icon(): Int = when (this) {
  FolderMode.Audiobooks -> UiR.drawable.ic_mage_folder
  FolderMode.SingleBook -> UiR.drawable.ic_mage_file
  FolderMode.Authors -> UiR.drawable.ic_mage_server
}

@StringRes
private fun FolderMode.title(): Int {
  return when (this) {
    FolderMode.Audiobooks -> StringsR.string.folder_mode_root
    FolderMode.SingleBook -> StringsR.string.folder_mode_single
    FolderMode.Authors -> StringsR.string.folder_mode_author
  }
}

@Composable
@Preview
private fun FolderModeSelectionCardPreview() {
  VoiceTheme {
    FolderModeSelectionCard(
      onFolderModeSelect = {},
      selectedFolderMode = FolderMode.Audiobooks,
    )
  }
}
