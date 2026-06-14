package voice.features.bookOverview.views

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun SettingsIcon(onSettingsClick: () -> Unit) {
  IconButton(onSettingsClick) {
    Icon(
      painter = painterResource(UiR.drawable.ic_mage_settings),
      contentDescription = stringResource(StringsR.string.action_settings),
    )
  }
}
