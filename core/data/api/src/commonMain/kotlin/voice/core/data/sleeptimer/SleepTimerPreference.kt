package voice.core.data.sleeptimer

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
public data class SleepTimerPreference(
  /**
   * The custom sleep time duration
   */
  val duration: Duration,
  /**
   * If the sleep timer should be automatically enabled between [autoSleepStartTime] and [autoSleepEndTime]
   */
  val autoSleepTimerEnabled: Boolean,
  val persistentSleepTimerEnabled: Boolean = false,
  val autoSleepStartTime: LocalTime,
  val autoSleepEndTime: LocalTime,
) {

  public companion object {
    public val Default: SleepTimerPreference = SleepTimerPreference(
      autoSleepTimerEnabled = false,
      persistentSleepTimerEnabled = false,
      autoSleepStartTime = LocalTime(22, 0),
      autoSleepEndTime = LocalTime(6, 0),
      duration = 20.minutes,
    )
  }
}
