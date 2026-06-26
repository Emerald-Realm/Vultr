package voice.features.settings.views
import voice.core.ui.RavenTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.ui.R as UiR

@Composable
internal fun SettingsRow(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  value: String? = null,
  trailing: SettingsRowTrailing = SettingsRowTrailing.None,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 0.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      fontSize = 15.sp,
      letterSpacing = (-0.075).sp,
      color = RavenTheme.colors.subTitle,
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (value != null) {
        Text(
          text = value,
          fontSize = 15.sp,
          letterSpacing = (-0.075).sp,
          color = RavenTheme.colors.support,
        )
      }
      when (trailing) {
        SettingsRowTrailing.Dots -> {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_dots),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = RavenTheme.colors.icon,
          )
        }
        SettingsRowTrailing.ExternalLink -> {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_arrow_up_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = RavenTheme.colors.icon,
          )
        }
        SettingsRowTrailing.None -> {}
      }
    }
  }
}

internal enum class SettingsRowTrailing {
  None, Dots, ExternalLink,
}
