package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.ui.PlayButton
import voice.core.ui.RavenTheme
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun PlaybackRow(
  playing: Boolean,
  showPreviousNext: Boolean,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onSkipToNext: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 28.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    if (showPreviousNext) {
      TransportIcon(
        res = UiR.drawable.ic_mage_previous_fill,
        size = 22.dp,
        description = stringResource(R.string.previous_track),
        onClick = onSkipToPrevious,
      )
    }
    TransportIcon(
      res = UiR.drawable.ic_mage_refresh_reverse,
      size = 34.dp,
      description = stringResource(R.string.rewind),
      onClick = onRewindClick,
    )
    PlayButton(playing = playing, fabSize = 68.dp, iconSize = 32.dp, onPlayClick = onPlayClick)
    TransportIcon(
      res = UiR.drawable.ic_mage_refresh,
      size = 34.dp,
      description = stringResource(R.string.fast_forward),
      onClick = onFastForwardClick,
    )
    if (showPreviousNext) {
      TransportIcon(
        res = UiR.drawable.ic_mage_next_fill,
        size = 22.dp,
        description = stringResource(R.string.next_track),
        onClick = onSkipToNext,
      )
    }
  }
}

@Composable
private fun TransportIcon(
  res: Int,
  size: androidx.compose.ui.unit.Dp,
  description: String,
  onClick: () -> Unit,
) {
  Icon(
    painter = painterResource(res),
    contentDescription = description,
    tint = RavenTheme.colors.title,
    modifier = Modifier
      .size(size)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = 28.dp),
        onClick = onClick,
      ),
  )
}
