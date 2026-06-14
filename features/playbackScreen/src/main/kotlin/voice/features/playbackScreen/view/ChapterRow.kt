package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.ui.RavenTheme
import voice.core.ui.R as UiR

@Composable
internal fun ChapterRow(
  chapterName: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 24.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(UiR.drawable.ic_mage_chevron_down),
      modifier = Modifier.size(18.dp),
      contentDescription = null,
      tint = RavenTheme.colors.subTitle,
    )
    Spacer(modifier = Modifier.size(10.dp))
    Text(
      text = chapterName,
      fontSize = 18.sp,
      letterSpacing = (-0.09).sp,
      color = RavenTheme.colors.subTitle,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
