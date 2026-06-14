package voice.features.settings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.data.ThemeMode
import voice.core.ui.RavenTheme
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun themeModeLabel(themeMode: ThemeMode): String {
  return stringResource(
    when (themeMode) {
      ThemeMode.FollowSystem -> StringsR.string.pref_theme_follow_system
      ThemeMode.Light -> StringsR.string.pref_theme_light
      ThemeMode.Dark -> StringsR.string.pref_theme_dark
    },
  )
}

@Composable
internal fun ThemeRow(
  themeMode: ThemeMode,
  onClick: () -> Unit,
) {
  SettingsRow(
    label = stringResource(StringsR.string.pref_theme_title),
    value = themeModeLabel(themeMode),
    trailing = SettingsRowTrailing.Dots,
    onClick = onClick,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemePickerDialog(
  selected: ThemeMode,
  onSelect: (ThemeMode) -> Unit,
  onDismiss: () -> Unit,
) {
  var currentSelection by remember { mutableStateOf(selected) }
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .navigationBarsPadding()
        .padding(bottom = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = stringResource(StringsR.string.pref_theme_title),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
        color = RavenTheme.colors.title,
      )
      Spacer(Modifier.height(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        ThemeMode.entries.forEach { mode ->
          ThemeOptionCard(
            modifier = Modifier.weight(1f),
            mode = mode,
            selected = currentSelection == mode,
            onClick = { currentSelection = mode },
          )
        }
      }
      Spacer(Modifier.height(24.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable {
            onSelect(currentSelection)
            onDismiss()
          },
        shape = RoundedCornerShape(12.dp),
        color = RavenTheme.colors.primary,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Save Settings",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            letterSpacing = (-0.08).sp,
          )
        }
      }
    }
  }
}

@Composable
private fun ThemeOptionCard(
  mode: ThemeMode,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .clickable(onClick = onClick),
      shape = RoundedCornerShape(8.dp),
      border = BorderStroke(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.bgSecondary,
      ),
      color = if (selected) RavenTheme.colors.primaryFaint else RavenTheme.colors.bgModal,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ThemeLogo(mode)
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = themeModeLabel(mode),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = (-0.06).sp,
      textAlign = TextAlign.Center,
      color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.subTitle,
    )
  }
}

@Composable
private fun ThemeLogo(mode: ThemeMode) {
  val colors = RavenTheme.colors
  Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
    Icon(
      painter = painterResource(UiR.drawable.ic_raven_logo),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      // "Dark" reads as a black bird in light mode; flip to white in dark mode so it stays visible.
      tint = if (mode == ThemeMode.Dark) colors.title else colors.primary,
    )
    if (mode == ThemeMode.FollowSystem) {
      // Right half rendered grey to signal "follows the system".
      Icon(
        painter = painterResource(UiR.drawable.ic_raven_logo),
        contentDescription = null,
        modifier = Modifier
          .fillMaxSize()
          .drawWithContent {
            clipRect(left = size.width / 2f) { this@drawWithContent.drawContent() }
          },
        tint = colors.inactive,
      )
    }
  }
}
