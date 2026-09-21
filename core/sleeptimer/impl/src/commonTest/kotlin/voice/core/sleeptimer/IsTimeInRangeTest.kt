package voice.core.sleeptimer

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class IsTimeInRangeTest {
  @Test
  fun rangeWrapsAroundMidnight() {
    val startTime = LocalTime(22, 0)
    val endTime = LocalTime(6, 0)
    isTimeInRange(LocalTime(5, 59), startTime, endTime) shouldBe true
    isTimeInRange(LocalTime(6, 0), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(12, 30), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(22, 0), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(22, 1), startTime, endTime) shouldBe true
  }

  @Test
  fun startAndEndOnSameDay() {
    val startTime = LocalTime(6, 0)
    val endTime = LocalTime(22, 0)
    isTimeInRange(LocalTime(5, 59), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(6, 1), startTime, endTime) shouldBe true
    isTimeInRange(LocalTime(12, 5), startTime, endTime) shouldBe true
    isTimeInRange(LocalTime(22, 0), startTime, endTime) shouldBe false
  }

  @Test
  fun invalidRange() {
    val startTime = LocalTime(6, 0)
    val endTime = LocalTime(6, 0)
    isTimeInRange(LocalTime(6, 0), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(6, 1), startTime, endTime) shouldBe false
    isTimeInRange(LocalTime(12, 5), startTime, endTime) shouldBe false
  }
}
