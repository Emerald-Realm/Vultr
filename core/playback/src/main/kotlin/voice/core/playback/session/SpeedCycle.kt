package voice.core.playback.session

import androidx.media3.session.CommandButton

internal object SpeedCycle {

  val presets = listOf(1.0F, 1.2F, 1.5F, 1.8F, 2.0F)

  fun next(current: Float): Float {
    return presets.firstOrNull { it > current + 0.01F } ?: presets.first()
  }

  fun icon(speed: Float): Int {
    return when {
      speed < 0.65F -> CommandButton.ICON_PLAYBACK_SPEED_0_5
      speed < 0.9F -> CommandButton.ICON_PLAYBACK_SPEED_0_8
      speed < 1.1F -> CommandButton.ICON_PLAYBACK_SPEED_1_0
      speed < 1.35F -> CommandButton.ICON_PLAYBACK_SPEED_1_2
      speed < 1.65F -> CommandButton.ICON_PLAYBACK_SPEED_1_5
      speed < 1.9F -> CommandButton.ICON_PLAYBACK_SPEED_1_8
      else -> CommandButton.ICON_PLAYBACK_SPEED_2_0
    }
  }
}
