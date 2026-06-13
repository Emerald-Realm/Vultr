package voice.features.bookOverview.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.ui.ImmutableFile
import voice.core.ui.PlayButton
import voice.core.ui.R as UiR

/**
 * Renders the book cover as a faux 2D book sitting in front of a disc, both using
 * the book's cover art. A soft shadow behind the cover and a thin spine on its
 * left edge create the 2D book effect; the disc peeks out to the right like a
 * sleeve. A play button is overlaid in the bottom-right.
 */
@Composable
internal fun BookCoverArt(
  cover: ImmutableFile?,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(CoverArtBackground)
      .padding(horizontal = 24.dp, vertical = 28.dp),
    contentAlignment = Alignment.Center,
  ) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
      val coverSize = maxWidth * 0.78f
      val discSize = coverSize * 0.94f
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(coverSize),
      ) {
        // Disc behind, peeking out to the right (dimmed cover art).
        AsyncImage(
          model = cover?.file,
          placeholder = painterResource(UiR.drawable.album_art),
          error = painterResource(UiR.drawable.album_art),
          contentScale = ContentScale.Crop,
          contentDescription = null,
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .size(discSize)
            .clip(CircleShape)
            .drawWithContent {
              drawContent()
              drawRect(Color.Black.copy(alpha = 0.45f))
            },
        )

        // The book cover itself, raised with a shadow and a spine on the left.
        Box(
          modifier = Modifier
            .align(Alignment.CenterStart)
            .size(coverSize)
            .shadow(elevation = 18.dp, shape = RoundedCornerShape(10.dp), clip = false)
            .clip(RoundedCornerShape(10.dp)),
        ) {
          AsyncImage(
            model = cover?.file,
            placeholder = painterResource(UiR.drawable.album_art),
            error = painterResource(UiR.drawable.album_art),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
          )
          // Spine: a subtle darkening on the left edge for the 2D book look.
          Box(
            modifier = Modifier
              .align(Alignment.CenterStart)
              .fillMaxHeight()
              .width(coverSize * 0.06f)
              .background(
                Brush.horizontalGradient(
                  listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent),
                ),
              ),
          )
        }

        PlayButton(
          playing = false,
          fabSize = 56.dp,
          iconSize = 28.dp,
          onPlayClick = onPlayClick,
          modifier = Modifier.align(Alignment.BottomEnd),
        )
      }
    }
  }
}

private val CoverArtBackground = Color(0xFF1B1B1D)
