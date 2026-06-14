package voice.features.playbackScreen.view

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
internal fun AppBarTitle(
  title: String,
  author: String? = null,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = title,
      modifier = Modifier.basicMarquee(),
      fontSize = 24.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = (-0.12).sp,
      textAlign = TextAlign.Center,
      color = Color.Black,
    )
    if (!author.isNullOrBlank()) {
      Text(
        text = author,
        fontSize = 12.sp,
        letterSpacing = (-0.06).sp,
        color = Color(0xFF627193),
        textAlign = TextAlign.Center,
      )
    }
  }
}
