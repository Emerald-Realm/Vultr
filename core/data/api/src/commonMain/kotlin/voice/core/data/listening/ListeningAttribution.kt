package voice.core.data.listening

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Splits a measured listening interval by local calendar dates. The measurement itself must come
 * from a monotonic clock; wall time is only used to decide which date gets which part.
 *
 * A wall-clock jump is not listening. If the wall interval disagrees with the measured interval, the
 * whole measurement is attributed to the end date instead of inventing time on other days.
 */
public fun attributeListening(
  elapsedMs: Long,
  startedAt: Instant,
  endedAt: Instant,
  timeZone: TimeZone,
): Map<LocalDate, Long> {
  if (elapsedMs <= 0L) return emptyMap()
  val startDate = startedAt.toLocalDateTime(timeZone).date
  val endDate = endedAt.toLocalDateTime(timeZone).date
  if (startDate == endDate) return mapOf(endDate to elapsedMs)

  val wallDuration = endedAt - startedAt
  if (wallDuration <= Duration.ZERO || abs(wallDuration.inWholeMilliseconds - elapsedMs) > CLOCK_CHANGE_TOLERANCE_MS) {
    return mapOf(endDate to elapsedMs)
  }

  val allocations = mutableMapOf<LocalDate, Long>()
  var allocated = 0L
  var date = startDate
  while (date <= endDate) {
    val dayStart = date.atStartOfDayIn(timeZone)
    val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
    val from = maxOf(startedAt, dayStart)
    val to = minOf(endedAt, dayEnd)
    if (to > from) {
      val portion =
        (
          elapsedMs.toDouble() * (to - from).inWholeMilliseconds /
            wallDuration.inWholeMilliseconds
        ).roundToLong()
      if (portion > 0L) {
        allocations[date] = portion
        allocated += portion
      }
    }
    date = date.plus(1, DateTimeUnit.DAY)
  }
  val remainder = elapsedMs - allocated
  if (remainder != 0L) {
    allocations[endDate] = (allocations[endDate] ?: 0L) + remainder
  }
  return allocations.filterValues { it > 0L }
}

private val CLOCK_CHANGE_TOLERANCE_MS = 2.minutes.inWholeMilliseconds
