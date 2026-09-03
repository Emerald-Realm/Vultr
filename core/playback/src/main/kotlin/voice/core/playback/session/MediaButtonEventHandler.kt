package voice.core.playback.session

import android.view.KeyEvent
import dev.zacsweers.metro.Inject
import voice.core.logging.api.Logger
import voice.core.playback.player.VoicePlayer

/**
 * Distinguishes tap from hold on hardware next/previous buttons (bluetooth, steering wheel):
 * a tap keeps the default time seek while a hold skips by chapter. Hold is detected through
 * key-repeat events, so head units that never forward long-presses simply keep the tap behavior.
 */
@Inject
class MediaButtonEventHandler(private val player: VoicePlayer) {

  private var activeKeyCode: Int? = null
  private var holdFired = false

  /** Returns true if the event was consumed and default handling must be skipped. */
  fun onKeyEvent(
    event: KeyEvent,
    isAndroidAuto: Boolean,
  ): Boolean {
    return when (event.keyCode) {
      KeyEvent.KEYCODE_MEDIA_NEXT,
      KeyEvent.KEYCODE_MEDIA_PREVIOUS,
      -> onNextPreviousEvent(event)
      KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
      KeyEvent.KEYCODE_MEDIA_REWIND,
      -> if (isAndroidAuto) {
        // Some head units send these while next / previous is held down.
        onFastForwardRewindEvent(event)
      } else {
        false
      }
      else -> false
    }
  }

  private fun onNextPreviousEvent(event: KeyEvent): Boolean {
    val forward = event.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
    when (event.action) {
      KeyEvent.ACTION_DOWN -> {
        if (event.repeatCount == 0) {
          activeKeyCode = event.keyCode
          holdFired = false
        } else if (!holdFired) {
          Logger.d("hold detected for keyCode=${event.keyCode}, skipping chapter")
          holdFired = true
          if (forward) {
            player.forceSeekToNext()
          } else {
            player.forceSeekToPrevious()
          }
        }
      }
      KeyEvent.ACTION_UP -> {
        val wasHold = holdFired
        val canceled = event.isCanceled
        activeKeyCode = null
        holdFired = false
        if (!wasHold && !canceled) {
          if (forward) {
            player.seekForward()
          } else {
            player.seekBack()
          }
        }
      }
    }
    return true
  }

  private fun onFastForwardRewindEvent(event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
      Logger.d("fast forward / rewind from car, skipping chapter")
      if (event.keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
        player.forceSeekToNext()
      } else {
        player.forceSeekToPrevious()
      }
    }
    return true
  }
}
