package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun SkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  Icon(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
      .size(32.dp),
    painter = painterResource(
      if (forward) UiR.drawable.ic_mage_refresh else UiR.drawable.ic_mage_refresh_reverse,
    ),
    contentDescription = stringResource(
      id = if (forward) {
        R.string.fast_forward
      } else {
        R.string.rewind
      },
    ),
  )
}
