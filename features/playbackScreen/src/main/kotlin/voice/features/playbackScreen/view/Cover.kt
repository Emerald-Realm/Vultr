package voice.features.playbackScreen.view

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.ui.ImmutableFile
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun Cover(
  onDoubleClick: () -> Unit,
  cover: ImmutableFile?,
) {
  AsyncImage(
    modifier = Modifier
      .fillMaxSize()
      .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), clip = false)
      .clip(RoundedCornerShape(12.dp))
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = { onDoubleClick() },
        )
      },
    contentScale = ContentScale.Crop,
    model = cover?.file,
    placeholder = painterResource(id = UiR.drawable.album_art),
    error = painterResource(id = UiR.drawable.album_art),
    contentDescription = stringResource(id = StringsR.string.cover),
  )
}
