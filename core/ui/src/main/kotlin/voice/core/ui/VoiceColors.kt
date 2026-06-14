package voice.core.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import voice.core.ui.theme.DarkRavenColors
import voice.core.ui.theme.LightRavenColors
import voice.core.ui.theme.RavenColors

internal val BrandBlue = LightRavenColors.primary

// Material3 schemes are derived from the Raven design tokens so MaterialTheme.colorScheme stays
// consistent with the token layer in both light and dark mode.
internal fun lightSchemeFrom(c: RavenColors) = lightColorScheme(
  primary = c.primary,
  onPrimary = c.white,
  primaryContainer = c.primaryFaint,
  onPrimaryContainer = c.primaryDark,
  secondary = c.primary,
  onSecondary = c.white,
  secondaryContainer = c.primaryFaint,
  onSecondaryContainer = c.primaryDark,
  tertiary = c.primary,
  onTertiary = c.white,
  background = c.bgMain,
  onBackground = c.title,
  surface = c.bgMain,
  onSurface = c.title,
  surfaceVariant = c.bgStyle,
  onSurfaceVariant = c.support,
  outline = c.borderStrong,
  outlineVariant = c.borderAvg,
  error = c.errorBase,
)

internal fun darkSchemeFrom(c: RavenColors) = darkColorScheme(
  primary = c.primary,
  onPrimary = c.white,
  primaryContainer = c.primaryFaint,
  onPrimaryContainer = c.primaryDark,
  secondary = c.primary,
  onSecondary = c.white,
  secondaryContainer = c.primaryFaint,
  onSecondaryContainer = c.primaryDark,
  tertiary = c.primary,
  onTertiary = c.white,
  background = c.bgMain,
  onBackground = c.title,
  surface = c.bgModal,
  onSurface = c.title,
  surfaceVariant = c.bgStyle,
  onSurfaceVariant = c.support,
  outline = c.borderStrong,
  outlineVariant = c.borderAvg,
  error = c.errorBase,
)

internal val LightColorScheme = lightSchemeFrom(LightRavenColors)
internal val DarkColorScheme = darkSchemeFrom(DarkRavenColors)
