package voice.core.playback.session

import android.view.KeyEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import voice.core.logging.api.LogWriter
import voice.core.logging.api.Logger
import voice.core.playback.player.VoicePlayer

@RunWith(AndroidJUnit4::class)
class MediaButtonEventHandlerTest {

  init {
    Logger.install(
      object : LogWriter {
        override fun log(
          severity: Logger.Severity,
          message: String,
          throwable: Throwable?,
        ) {
          println("$severity: $message")
          throwable?.printStackTrace()
        }
      },
    )
  }

  private val player = mockk<VoicePlayer>(relaxed = true)
  private val handler = MediaButtonEventHandler(player)

  @Test
  fun `tap on next seeks forward once`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.seekForward() }
    verify(exactly = 0) { player.forceSeekToNext() }
  }

  @Test
  fun `tap on previous seeks back once`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_PREVIOUS), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_PREVIOUS), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.seekBack() }
    verify(exactly = 0) { player.forceSeekToPrevious() }
  }

  @Test
  fun `hold on next skips a single chapter and does not time seek`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT, repeat = 1), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT, repeat = 2), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.forceSeekToNext() }
    verify(exactly = 0) { player.seekForward() }
  }

  @Test
  fun `hold on previous skips a single chapter and does not time seek`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_PREVIOUS), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_PREVIOUS, repeat = 1), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_PREVIOUS), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.forceSeekToPrevious() }
    verify(exactly = 0) { player.seekBack() }
  }

  @Test
  fun `canceled up does nothing`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(canceledUp(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    verify(exactly = 0) { player.seekForward() }
    verify(exactly = 0) { player.forceSeekToNext() }
  }

  @Test
  fun `bare up without a preceding down acts as a tap`() {
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.seekForward() }
  }

  @Test
  fun `taps after a hold are independent`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT, repeat = 1), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_NEXT), isAndroidAuto = false) shouldBe true

    verify(exactly = 1) { player.forceSeekToNext() }
    verify(exactly = 1) { player.seekForward() }
  }

  @Test
  fun `fast forward from android auto skips a single chapter`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD), isAndroidAuto = true) shouldBe true
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, repeat = 1), isAndroidAuto = true) shouldBe true
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD), isAndroidAuto = true) shouldBe true

    verify(exactly = 1) { player.forceSeekToNext() }
  }

  @Test
  fun `rewind from android auto skips a single chapter back`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_REWIND), isAndroidAuto = true) shouldBe true

    verify(exactly = 1) { player.forceSeekToPrevious() }
  }

  @Test
  fun `fast forward from other controllers is not consumed`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD), isAndroidAuto = false) shouldBe false

    verify(exactly = 0) { player.forceSeekToNext() }
  }

  @Test
  fun `play is not consumed`() {
    handler.onKeyEvent(down(KeyEvent.KEYCODE_MEDIA_PLAY), isAndroidAuto = false) shouldBe false
    handler.onKeyEvent(up(KeyEvent.KEYCODE_MEDIA_PLAY), isAndroidAuto = false) shouldBe false
  }

  private fun down(
    keyCode: Int,
    repeat: Int = 0,
  ): KeyEvent = KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, repeat)

  private fun up(keyCode: Int): KeyEvent = KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0)

  private fun canceledUp(keyCode: Int): KeyEvent = KeyEvent(
    0,
    0,
    KeyEvent.ACTION_UP,
    keyCode,
    0,
    0,
    KeyEvent.KEYCODE_UNKNOWN,
    0,
    KeyEvent.FLAG_CANCELED,
  )
}
