package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.strings.R
import voice.core.ui.RavenTheme
import voice.core.ui.formatTime
import voice.features.playbackScreen.BookPlayViewState
import voice.core.ui.R as UiR

@Composable
internal fun PlayerActionsBar(
  speedText: String,
  speedActive: Boolean,
  sleepTimerState: BookPlayViewState.SleepTimerViewState,
  onSpeedClick: () -> Unit,
  onBookmarksClick: () -> Unit,
  onHistoryClick: () -> Unit,
  onSleepClick: () -> Unit,
  modifier: Modifier = Modifier,
  applyNavigationBarsPadding: Boolean = false,
) {
  val colors = RavenTheme.colors
  Row(
    modifier = modifier
      .fillMaxWidth()
      .then(if (applyNavigationBarsPadding) Modifier.navigationBarsPadding() else Modifier)
      .padding(horizontal = 24.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ActionItem(
      label = stringResource(id = R.string.player_action_speed),
      labelColor = if (speedActive) colors.primary else colors.caption,
      onClick = onSpeedClick,
    ) {
      Text(
        text = speedText,
        fontSize = 16.sp,
        letterSpacing = (-0.08).sp,
        color = if (speedActive) colors.primary else colors.title,
      )
    }
    ActionItem(
      painter = painterResource(UiR.drawable.ic_mage_bookmark),
      label = stringResource(id = R.string.player_action_bookmarks),
      onClick = onBookmarksClick,
    )
    ActionItem(
      painter = painterResource(UiR.drawable.ic_mage_clock),
      label = stringResource(id = R.string.history_title),
      onClick = onHistoryClick,
    )
    val sleepDuration = sleepTimerState as? BookPlayViewState.SleepTimerViewState.Enabled.WithDuration
    val sleepActive = sleepTimerState != BookPlayViewState.SleepTimerViewState.Disabled
    ActionItem(
      label = stringResource(id = R.string.player_action_sleep),
      labelColor = if (sleepActive) colors.primary else colors.caption,
      onClick = onSleepClick,
    ) {
      if (sleepDuration != null) {
        Text(
          text = formatTime(sleepDuration.leftDuration.inWholeMilliseconds),
          fontSize = 14.sp,
          letterSpacing = (-0.07).sp,
          color = colors.primary,
        )
      } else {
        Icon(
          painter = painterResource(UiR.drawable.ic_mage_moon),
          contentDescription = null,
          modifier = Modifier.size(24.dp),
          tint = if (sleepActive) colors.primary else colors.title,
        )
      }
    }
  }
}

@Composable
private fun ActionItem(
  painter: Painter,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = RavenTheme.colors
  ActionItem(label = label, labelColor = colors.caption, onClick = onClick, modifier = modifier) {
    Icon(
      painter = painter,
      contentDescription = label,
      modifier = Modifier.size(24.dp),
      tint = colors.title,
    )
  }
}

@Composable
private fun ActionItem(
  label: String,
  labelColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  value: @Composable () -> Unit,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier.height(28.dp),
      contentAlignment = Alignment.Center,
    ) {
      value()
    }
    Text(
      text = label,
      modifier = Modifier.padding(top = 4.dp),
      fontSize = 12.sp,
      letterSpacing = (-0.06).sp,
      color = labelColor,
    )
  }
}
