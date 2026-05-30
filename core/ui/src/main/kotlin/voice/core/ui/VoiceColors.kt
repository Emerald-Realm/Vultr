package voice.core.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val BrandBlue = Color(0xFF457CFA)

internal val LightColorScheme = lightColorScheme(
  primary = BrandBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFF6F8FF),
  onPrimaryContainer = Color(0xFF13234E),
  secondary = BrandBlue,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF6F8FF),
  onSecondaryContainer = Color(0xFF13234E),
  tertiary = BrandBlue,
  onTertiary = Color.White,
  background = Color.White,
  onBackground = Color(0xFF13234E),
  surface = Color.White,
  onSurface = Color(0xFF13234E),
  surfaceVariant = Color(0xFFF3F5F6),
  onSurfaceVariant = Color(0xFF676B83),
  outline = Color(0xFFC2CBD6),
  outlineVariant = Color(0xFFE3E8EF),
)

internal val DarkColorScheme = darkColorScheme(
  primary = BrandBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF1B2C57),
  onPrimaryContainer = Color(0xFFD6E2FF),
  secondary = BrandBlue,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF1B2C57),
  onSecondaryContainer = Color(0xFFD6E2FF),
  tertiary = BrandBlue,
  onTertiary = Color.White,
  background = Color(0xFF101216),
  onBackground = Color(0xFFE4E6EE),
  surface = Color(0xFF181B20),
  onSurface = Color(0xFFE4E6EE),
  surfaceVariant = Color(0xFF2A2E37),
  onSurfaceVariant = Color(0xFFA7ACBF),
  outline = Color(0xFF3C424E),
  outlineVariant = Color(0xFF2A2E37),
)
