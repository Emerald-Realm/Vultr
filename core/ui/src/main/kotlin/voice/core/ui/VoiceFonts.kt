@file:OptIn(ExperimentalTextApi::class)

package voice.core.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

private fun hostGroteskFont(weight: FontWeight): Font {
  return Font(
    resId = R.font.host_grotesk,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
  )
}

val HostGrotesk: FontFamily = FontFamily(
  hostGroteskFont(FontWeight.Light),
  hostGroteskFont(FontWeight.Normal),
  hostGroteskFont(FontWeight.Medium),
  hostGroteskFont(FontWeight.SemiBold),
  hostGroteskFont(FontWeight.Bold),
)

val VoiceTypography: Typography = Typography().run {
  copy(
    displayLarge = displayLarge.copy(fontFamily = HostGrotesk),
    displayMedium = displayMedium.copy(fontFamily = HostGrotesk),
    displaySmall = displaySmall.copy(fontFamily = HostGrotesk),
    headlineLarge = headlineLarge.copy(fontFamily = HostGrotesk),
    headlineMedium = headlineMedium.copy(fontFamily = HostGrotesk),
    headlineSmall = headlineSmall.copy(fontFamily = HostGrotesk),
    titleLarge = titleLarge.copy(fontFamily = HostGrotesk),
    titleMedium = titleMedium.copy(fontFamily = HostGrotesk),
    titleSmall = titleSmall.copy(fontFamily = HostGrotesk),
    bodyLarge = bodyLarge.copy(fontFamily = HostGrotesk),
    bodyMedium = bodyMedium.copy(fontFamily = HostGrotesk),
    bodySmall = bodySmall.copy(fontFamily = HostGrotesk),
    labelLarge = labelLarge.copy(fontFamily = HostGrotesk),
    labelMedium = labelMedium.copy(fontFamily = HostGrotesk),
    labelSmall = labelSmall.copy(fontFamily = HostGrotesk),
  )
}
