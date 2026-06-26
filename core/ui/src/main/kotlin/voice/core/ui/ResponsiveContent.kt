package voice.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Caps content at a comfortable reading width and centers it horizontally.
 *
 * On phones this is a no-op (the screen is narrower than [maxWidth]); on tablets and other
 * wide screens it keeps text-heavy, single-column screens readable instead of stretching
 * them edge to edge.
 */
fun Modifier.readableContentWidth(maxWidth: Dp = 640.dp): Modifier =
  this
    .fillMaxWidth()
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = maxWidth)
