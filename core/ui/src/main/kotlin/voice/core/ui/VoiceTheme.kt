package voice.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import voice.core.ui.theme.DarkRavenColors
import voice.core.ui.theme.LightRavenColors
import voice.core.ui.theme.LocalRavenColors
import voice.core.ui.theme.RavenColors

@Composable
fun VoiceTheme(content: @Composable () -> Unit) {
  val dark = isDarkTheme()
  val ravenColors = if (dark) DarkRavenColors else LightRavenColors
  CompositionLocalProvider(LocalRavenColors provides ravenColors) {
    MaterialTheme(
      colorScheme = if (dark) DarkColorScheme else LightColorScheme,
      typography = VoiceTypography,
    ) {
      content()
    }
  }
}

/** Entry point for the Raven design tokens, e.g. `RavenTheme.colors.title`. */
object RavenTheme {
  val colors: RavenColors
    @Composable
    @ReadOnlyComposable
    get() = LocalRavenColors.current
}
