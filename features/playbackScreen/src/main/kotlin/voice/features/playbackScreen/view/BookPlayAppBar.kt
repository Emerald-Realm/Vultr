package voice.features.playbackScreen.view

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import voice.features.playbackScreen.BookPlayViewState

/**
 * App bar is the same in portrait and landscape so tablets get full feature parity with phone:
 * close + overflow (skip silence, volume boost, add bookmark). Transport actions live in
 * [PlayerActionsBar], not the top bar.
 */
@Composable
internal fun BookPlayAppBar(
  viewState: BookPlayViewState,
  onAddBookmarkClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onCloseClick: () -> Unit,
) {
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
