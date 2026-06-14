package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun CloseIcon(onCloseClick: () -> Unit) {
  IconButton(onClick = onCloseClick) {
    Icon(
      painter = painterResource(UiR.drawable.ic_mage_chevron_down),
      contentDescription = stringResource(id = R.string.close),
      modifier = Modifier.size(24.dp),
    )
  }
}
