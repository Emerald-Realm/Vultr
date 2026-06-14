package voice.features.bookOverview.views

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A small animated equalizer/sound-wave shown on the currently-playing book while it is
 * playing. Hidden by the caller when paused.
 */
@Composable
internal fun PlayingIndicator(
  modifier: Modifier = Modifier,
  barColor: Color = Color.White,
) {
  val transition = rememberInfiniteTransition(label = "equalizer")
  val delays = listOf(0, 180, 90, 270)
  Row(
    modifier = modifier.height(14.dp),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    delays.forEach { delay ->
      val fraction by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 500, delayMillis = delay),
          repeatMode = RepeatMode.Reverse,
        ),
        label = "bar",
      )
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .fillMaxHeight(fraction)
          .clip(RoundedCornerShape(2.dp))
          .background(barColor),
      )
    }
  }
}
