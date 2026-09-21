package voice.core.sleeptimer

import kotlinx.datetime.LocalTime

fun isTimeInRange(
  currentTime: LocalTime,
  startTime: LocalTime,
  endTime: LocalTime,
): Boolean {
  return if (startTime <= endTime) {
    // Standard case, start and end on the same day
    currentTime > startTime && currentTime < endTime
  } else {
    // Range wraps around midnight
    currentTime > startTime || currentTime < endTime
  }
}
