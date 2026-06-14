package voice.features.playbackScreen.view

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.features.playbackScreen.BookPlayViewState
import voice.core.ui.R as UiR

@Composable
internal fun BookPlayAppBar(
  viewState: BookPlayViewState,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onAddBookmarkClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onCloseClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  if (useLandscapeLayout) {
    val landscapeActions: @Composable RowScope.() -> Unit = {
      IconButton(onClick = onSleepTimerClick) {
        Icon(
          painter = painterResource(UiR.drawable.ic_mage_moon),
          contentDescription = stringResource(id = R.string.action_sleep),
          modifier = Modifier.size(24.dp),
        )
      }
      Box(
        modifier = Modifier
          .size(40.dp)
          .combinedClickable(
            onClick = onBookmarkClick,
            onLongClick = onBookmarkLongClick,
            indication = ripple(bounded = false, radius = 20.dp),
            interactionSource = remember { MutableInteractionSource() },
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(UiR.drawable.ic_mage_bookmark),
          contentDescription = stringResource(id = R.string.bookmark),
          modifier = Modifier.size(24.dp),
        )
      }
      OverflowMenu(
        skipSilence = viewState.skipSilence,
        onSkipSilenceClick = onSkipSilenceClick,
        onVolumeBoostClick = onVolumeBoostClick,
      )
    }
    TopAppBar(
      navigationIcon = { CloseIcon(onCloseClick) },
      actions = landscapeActions,
      title = {},
    )
  } else {
    CenterAlignedTopAppBar(
      navigationIcon = { CloseIcon(onCloseClick) },
      title = {},
      actions = {
        OverflowMenu(
          skipSilence = viewState.skipSilence,
          onSkipSilenceClick = onSkipSilenceClick,
          onVolumeBoostClick = onVolumeBoostClick,
          onAddBookmarkClick = onAddBookmarkClick,
        )
      },
    )
  }
}
