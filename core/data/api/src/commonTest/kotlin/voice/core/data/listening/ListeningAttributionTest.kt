package voice.core.data.listening

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListeningAttributionTest {
  private val utc = TimeZone.UTC

  @Test
  fun `a midnight crossing session is split between both local dates`() {
    val result =
      attributeListening(
        elapsedMs = 3_600_000L,
        startedAt = Instant.parse("2026-10-08T23:30:00Z"),
        endedAt = Instant.parse("2026-10-09T00:30:00Z"),
        timeZone = utc,
      )

    assertEquals(1_800_000L, result[kotlinx.datetime.LocalDate(2026, 10, 8)])
    assertEquals(1_800_000L, result[kotlinx.datetime.LocalDate(2026, 10, 9)])
  }

  @Test
  fun `a session crossing into a new month is split by month`() {
    val result =
      attributeListening(
        elapsedMs = 3_600_000L,
        startedAt = Instant.parse("2026-10-31T23:30:00Z"),
        endedAt = Instant.parse("2026-11-01T00:30:00Z"),
        timeZone = utc,
      )

    assertEquals(1_800_000L, result[kotlinx.datetime.LocalDate(2026, 10, 31)])
    assertEquals(1_800_000L, result[kotlinx.datetime.LocalDate(2026, 11, 1)])
  }

  @Test
  fun `a clock change attributes the measurement to the end date only`() {
    val result =
      attributeListening(
        elapsedMs = 60_000L,
        startedAt = Instant.parse("2026-10-08T12:00:00Z"),
        endedAt = Instant.parse("2026-10-08T14:00:00Z"),
        timeZone = utc,
      )

    assertEquals(mapOf(kotlinx.datetime.LocalDate(2026, 10, 8) to 60_000L), result)
  }

  @Test
  fun `a backwards clock change does not invent time on other dates`() {
    val result =
      attributeListening(
        elapsedMs = 60_000L,
        startedAt = Instant.parse("2026-10-09T01:00:00Z"),
        endedAt = Instant.parse("2026-10-08T23:00:00Z"),
        timeZone = utc,
      )

    assertEquals(mapOf(kotlinx.datetime.LocalDate(2026, 10, 8) to 60_000L), result)
  }

  @Test
  fun `non positive measurements are ignored`() {
    assertTrue(
      attributeListening(
        elapsedMs = 0L,
        startedAt = Instant.parse("2026-10-08T12:00:00Z"),
        endedAt = Instant.parse("2026-10-08T12:00:00Z"),
        timeZone = utc,
      ).isEmpty(),
    )
  }

  @Test
  fun `dates follow the local zone of the device`() {
    val tokyo = TimeZone.of("Asia/Tokyo")
    val result =
      attributeListening(
        elapsedMs = 60_000L,
        startedAt = Instant.parse("2026-10-07T14:30:00Z"),
        endedAt = Instant.parse("2026-10-07T14:31:00Z"),
        timeZone = tokyo,
      )

    assertEquals(mapOf(kotlinx.datetime.LocalDate(2026, 10, 7) to 60_000L), result)
    assertEquals(
      mapOf(kotlinx.datetime.LocalDate(2026, 10, 8) to 60_000L),
      attributeListening(
        elapsedMs = 60_000L,
        startedAt = Instant.parse("2026-10-07T15:00:00Z"),
        endedAt = Instant.parse("2026-10-07T15:01:00Z"),
        timeZone = tokyo,
      ),
    )
  }
}
