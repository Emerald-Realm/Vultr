package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import voice.core.ui.ImmutableFile

@Composable
internal fun CoverRow(
  cover: ImmutableFile?,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    Cover(onDoubleClick = onPlayClick, cover = cover)
  }
}
