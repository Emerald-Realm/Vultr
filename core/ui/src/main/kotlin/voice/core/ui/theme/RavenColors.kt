package voice.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens mirrored 1:1 from the Figma "Colors" variable collection (Light / Dark modes).
 * This is the single source of truth for colors in the app. Prefer these over hardcoded hex values
 * or [androidx.compose.material3.MaterialTheme] colors so both light and dark themes stay correct.
 *
 * Access via `RavenTheme.colors` inside a `VoiceTheme { }` scope.
 */
@Immutable
data class RavenColors(
  // Foreground
  val title: Color,
  val subTitle: Color,
  val support: Color,
  val caption: Color,
  val active: Color,
  val inactive: Color,
  val icon: Color,
  val inverse: Color,
  val input: Color,
  val placeholder: Color,
  // Background
  val bgMain: Color,
  val bgSecondary: Color,
  val bgTertiary: Color,
  val bgInverse: Color,
  val bgModal: Color,
  val bgActive: Color,
  val bgInactive: Color,
  val bgStyle: Color,
  // Border
  val borderStrong: Color,
  val borderAvg: Color,
  val borderMild: Color,
  // Brand
  val primary: Color,
  val primaryFaint: Color,
  val primaryLight: Color,
  val primaryDark: Color,
  // Basic
  val black: Color,
  val white: Color,
  // Feedback
  val successBase: Color,
  val successLight: Color,
  val successDark: Color,
  val errorBase: Color,
  val errorLight: Color,
  val errorDark: Color,
  val warningBase: Color,
  val warningLight: Color,
  val warningDark: Color,
  val isDark: Boolean,
)

internal val LightRavenColors = RavenColors(
  title = Color(0xFF000000),
  subTitle = Color(0xFF3D3D3D),
  support = Color(0xFF627193),
  caption = Color(0xFF949494),
  active = Color(0xFF000000),
  inactive = Color(0xFFABABAB),
  icon = Color(0xFF696969),
  inverse = Color(0xFFFFFFFF),
  input = Color(0xFFFFFFFF),
  placeholder = Color(0xFFABABAB),
  bgMain = Color(0xFFFFFFFF),
  bgSecondary = Color(0xFFF9FAFB),
  bgTertiary = Color(0xFFEDEFF3),
  bgInverse = Color(0xFF376757),
  bgModal = Color(0xFFFFFFFF),
  bgActive = Color(0xFFFFFFFF),
  bgInactive = Color(0xFFF0F2F4),
  bgStyle = Color(0xFFF3F5F6),
  borderStrong = Color(0xFFDBE0E6),
  borderAvg = Color(0xFFF3F5F6),
  borderMild = Color(0xFFF9FAFB),
  primary = Color(0xFF457CFA),
  primaryFaint = Color(0xFFF6F8FF),
  primaryLight = Color(0xFFA8BAFD),
  primaryDark = Color(0xFF022472),
  black = Color(0xFF000000),
  white = Color(0xFFFFFFFF),
  successBase = Color(0xFF26BD01),
  successLight = Color(0xFFD6FFCC),
  successDark = Color(0xFF146600),
  errorBase = Color(0xFFFF1A11),
  errorLight = Color(0xFFFFEFEE),
  errorDark = Color(0xFF660400),
  warningBase = Color(0xFFFF9811),
  warningLight = Color(0xFFFFF9F0),
  warningDark = Color(0xFF663A00),
  isDark = false,
)

internal val DarkRavenColors = RavenColors(
  title = Color(0xFFFFFFFF),
  subTitle = Color(0xFFD4D4D4),
  support = Color(0xFF9AA6C0),
  caption = Color(0xFF8A8A8A),
  active = Color(0xFFFFFFFF),
  inactive = Color(0xFF6B6B6B),
  icon = Color(0xFFB0B0B0),
  inverse = Color(0xFF0D0D0F),
  input = Color(0xFF1F2024),
  placeholder = Color(0xFF6B6B6B),
  bgMain = Color(0xFF00030A),
  bgSecondary = Color(0xFF18191C),
  bgTertiary = Color(0xFF222427),
  bgInverse = Color(0xFFCFE9DF),
  bgModal = Color(0xFF1A1B1E),
  bgActive = Color(0xFF1F2024),
  bgInactive = Color(0xFF16171A),
  bgStyle = Color(0xFF222427),
  borderStrong = Color(0xFF3A3F47),
  borderAvg = Color(0xFF2A2D31),
  borderMild = Color(0xFF222427),
  primary = Color(0xFF4C81FA),
  primaryFaint = Color(0xFF00114D),
  primaryLight = Color(0xFF6988FC),
  primaryDark = Color(0xFFB9CEFE),
  black = Color(0xFF000000),
  white = Color(0xFFFFFFFF),
  successBase = Color(0xFF3DDB1E),
  successLight = Color(0xFF0F2E08),
  successDark = Color(0xFFB6F5A8),
  errorBase = Color(0xFFFF5C54),
  errorLight = Color(0xFF2E1413),
  errorDark = Color(0xFFFFB3AE),
  warningBase = Color(0xFFFFB454),
  warningLight = Color(0xFF2E2410),
  warningDark = Color(0xFFFFD9A8),
  isDark = true,
)

val LocalRavenColors = staticCompositionLocalOf { LightRavenColors }
