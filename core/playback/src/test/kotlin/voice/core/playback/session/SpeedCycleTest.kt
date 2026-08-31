package voice.core.playback.session

import androidx.media3.session.CommandButton
import io.kotest.matchers.shouldBe
import org.junit.Test

class SpeedCycleTest {

  @Test
  fun `cycles through the presets and wraps around`() {
    SpeedCycle.next(1.0F) shouldBe 1.2F
    SpeedCycle.next(1.2F) shouldBe 1.5F
    SpeedCycle.next(1.5F) shouldBe 1.8F
    SpeedCycle.next(1.8F) shouldBe 2.0F
    SpeedCycle.next(2.0F) shouldBe 1.0F
  }

  @Test
  fun `snaps off-preset speeds to the next larger preset`() {
    SpeedCycle.next(0.9F) shouldBe 1.0F
    SpeedCycle.next(1.75F) shouldBe 1.8F
    SpeedCycle.next(3.5F) shouldBe 1.0F
  }

  @Test
  fun `maps speeds to the nearest icon`() {
    SpeedCycle.icon(0.5F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_0_5
    SpeedCycle.icon(0.8F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_0_8
    SpeedCycle.icon(1.0F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_1_0
    SpeedCycle.icon(1.2F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_1_2
    SpeedCycle.icon(1.5F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_1_5
    SpeedCycle.icon(1.75F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_1_8
    SpeedCycle.icon(1.8F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_1_8
    SpeedCycle.icon(2.0F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_2_0
    SpeedCycle.icon(3.5F) shouldBe CommandButton.ICON_PLAYBACK_SPEED_2_0
  }
}
