package voice.features.folderPicker.addcontent

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.logging.api.Logger
import voice.core.strings.R
import voice.core.ui.R as UiR
import voice.features.folderPicker.folderPicker.FileTypeSelection

@Composable
internal fun SelectFolderButtonRow(onAdd: (FileTypeSelection, Uri) -> Unit) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    val openDocumentLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        onAdd(FileTypeSelection.File, uri)
      }
    }
    val documentTreeLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
          onAdd(FileTypeSelection.Folder, uri)
        }
      }

    SelectFolderButton(
      modifier = Modifier.weight(1f),
      icon = UiR.drawable.ic_mage_folder,
      text = stringResource(id = R.string.select_folder_scan_folder),
      onClick = {
        try {
          documentTreeLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add folder")
        }
      },
    )
    SelectFolderButton(
      modifier = Modifier.weight(1f),
      icon = UiR.drawable.ic_mage_file,
      text = stringResource(id = R.string.select_folder_add_file),
      onClick = {
        try {
          openDocumentLauncher.launch(arrayOf("*/*"))
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add file")
        }
      },
    )
  }
}
