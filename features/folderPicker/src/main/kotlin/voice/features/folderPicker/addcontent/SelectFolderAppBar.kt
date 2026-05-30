package voice.features.folderPicker.addcontent

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun SelectFolderAppBar(onBack: () -> Unit) {
  TopAppBar(
    title = { },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          painter = painterResource(id = UiR.drawable.ic_mage_arrow_left),
          contentDescription = stringResource(id = R.string.close),
        )
      }
    },
  )
}
