package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import voice.core.ui.RavenTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun OverflowMenu(
  skipSilence: Boolean,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onAddBookmarkClick: (() -> Unit)? = null,
) {
  Box {
    var expanded by remember { mutableStateOf(false) }
    IconButton(
      onClick = { expanded = !expanded },
    ) {
      Icon(
        painter = painterResource(UiR.drawable.ic_mage_dots),
        contentDescription = stringResource(id = R.string.more),
        modifier = Modifier.size(24.dp),
      )
    }
    if (expanded) {
      Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = { expanded = false },
        properties = PopupProperties(focusable = true),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = RavenTheme.colors.bgModal,
          shadowElevation = 4.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, RavenTheme.colors.borderAvg),
        ) {
          Column(
            modifier = Modifier
              .width(200.dp)
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  expanded = false
                  onSkipSilenceClick()
                },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = stringResource(id = R.string.skip_silence),
                fontSize = 14.sp,
                letterSpacing = (-0.07).sp,
                color = RavenTheme.colors.subTitle,
              )
              Checkbox(
                checked = skipSilence,
                onCheckedChange = {
                  expanded = false
                  onSkipSilenceClick()
                },
                modifier = Modifier.size(20.dp),
              )
            }
            Text(
              text = stringResource(id = R.string.volume_boost),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  expanded = false
                  onVolumeBoostClick()
                },
              fontSize = 14.sp,
              letterSpacing = (-0.07).sp,
              color = RavenTheme.colors.subTitle,
            )
            if (onAddBookmarkClick != null) {
              Text(
                text = stringResource(id = R.string.add_bookmark),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    expanded = false
                    onAddBookmarkClick()
                  },
                fontSize = 14.sp,
                letterSpacing = (-0.07).sp,
                color = RavenTheme.colors.subTitle,
              )
            }
          }
        }
      }
    }
  }
}
