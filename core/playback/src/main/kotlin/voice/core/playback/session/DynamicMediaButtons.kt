package voice.core.playback.session

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import dev.zacsweers.metro.Inject
import voice.core.playback.player.VoicePlayer

@Inject
class DynamicMediaButtons(
  private val player: VoicePlayer,
  private val layout: MediaButtonLayout,
) {

  fun attachTo(session: MediaLibrarySession) {
    player.addListener(
      object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
          session.setMediaButtonPreferences(layout.buttons(playbackParameters.speed))
        }
      },
    )
  }
}
