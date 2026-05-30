package voice.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun VoiceTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = if (isDarkTheme()) DarkColorScheme else LightColorScheme,
    typography = VoiceTypography,
  ) {
    content()
  }
}
