package voice.features.bookOverview.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.ui.ImmutableFile
import voice.core.ui.PlayButton
import voice.core.ui.R as UiR

@Composable
internal fun BookCoverArt(
  cover: ImmutableFile?,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxWidth(),
  ) {
    AsyncImage(
      model = cover?.file,
      placeholder = painterResource(UiR.drawable.album_art),
      error = painterResource(UiR.drawable.album_art),
      contentScale = ContentScale.Crop,
      contentDescription = null,
      modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), clip = false)
        .clip(RoundedCornerShape(12.dp)),
    )
    PlayButton(
      playing = false,
      fabSize = 56.dp,
      iconSize = 28.dp,
      onPlayClick = onPlayClick,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(12.dp),
    )
  }
}
