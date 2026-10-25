package voice.core.data.listening

import voice.core.data.BookId
import voice.core.data.ChapterId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListeningStatsTest {
  private val bookId = BookId("book-1")
  private val chapter1 = ChapterId("chapter-1")
  private val chapter2 = ChapterId("chapter-2")
  private val chapters =
    listOf(
      ChapterDuration(chapter1, 1_000L),
      ChapterDuration(chapter2, 1_000L),
    )

  @Test
  fun `a seek to the end does not earn completion`() {
    val stats =
      ListeningStats()
        .addPlayedRange(bookId, chapter1, 0L, 50L)
        .addPlayedRange(bookId, chapter2, 0L, 50L)
        .addPlayedRange(bookId, chapter2, 1_950L, 2_000L)

    assertFalse(stats.isBookComplete(bookId, chapters))
    assertEquals(100L, stats.listenedMs(bookId, chapters))
  }

  @Test
  fun `replaying the same coverage does not duplicate it`() {
    val stats =
      ListeningStats()
        .addPlayedRange(bookId, chapter1, 0L, 600L)
        .addPlayedRange(bookId, chapter1, 100L, 700L)
        .addPlayedRange(bookId, chapter1, 0L, 600L)

    assertEquals(700L, stats.listenedMs(bookId, listOf(ChapterDuration(chapter1, 1_000L))))
  }

  @Test
  fun `a book is awarded at ninety five percent coverage`() {
    val almost = ListeningStats().addPlayedRange(bookId, chapter1, 0L, 949L)
    val complete = ListeningStats().addPlayedRange(bookId, chapter1, 0L, 950L)

    assertFalse(almost.isBookComplete(bookId, chapters.take(1)))
    assertTrue(complete.isBookComplete(bookId, chapters.take(1)))

    val awarded = complete.awardBookIfComplete(bookId, chapters.take(1))
    assertEquals(setOf(bookId.value), awarded.awardedBookIds)
    assertEquals(emptyMap(), awarded.playedRanges)

    val awardedAgain = awarded.awardBookIfComplete(bookId, chapters.take(1))
    assertEquals(awarded, awardedAgain)
  }

  @Test
  fun `unknown or zero durations never award completion`() {
    val stats = ListeningStats().addPlayedRange(bookId, chapter1, 0L, 20_000L)

    assertFalse(stats.isBookComplete(bookId, listOf(ChapterDuration(chapter1, 0L))))
    assertEquals(0L, stats.listenedMs(bookId, listOf(ChapterDuration(chapter1, 0L))))
    assertFalse(ListeningStats().isBookComplete(bookId, emptyList()))
  }

  @Test
  fun `coverage is clipped to the current chapter duration`() {
    val stats = ListeningStats().addPlayedRange(bookId, chapter1, 0L, 5_000L)

    assertEquals(1_000L, stats.listenedMs(bookId, listOf(ChapterDuration(chapter1, 1_000L))))
  }

  @Test
  fun `merged ranges stay ordered and joined when they touch`() {
    val merged =
      emptyList<PlayedRange>()
        .merge(PlayedRange(0L, 10L))
        .merge(PlayedRange(10L, 20L))
        .merge(PlayedRange(30L, 40L))

    assertEquals(listOf(PlayedRange(0L, 20L), PlayedRange(30L, 40L)), merged)
  }

  @Test
  fun `the rating window only sums local dates eight to fourteen`() {
    val stats =
      ListeningStats(
        dailyListenedMs =
          mapOf(
            "2026-10-07" to 1_000L,
            "2026-10-08" to 2_000L,
            "2026-10-14" to 3_000L,
            "2026-10-15" to 4_000L,
            "2026-09-08" to 5_000L,
          ),
      )

    assertEquals(5_000L, stats.listenedMsInRatingWindow(2026, 10))
  }

  @Test
  fun `adding listening only counts positive time`() {
    val stats =
      ListeningStats()
        .addListened(0L, mapOf("2026-10-08" to 100L))
        .addListened(-10L, mapOf("2026-10-08" to 100L))
        .addListened(1_000L, mapOf("2026-10-08" to 1_000L))

    assertEquals(1_000L, stats.lifetimeListenedMs)
    assertEquals(1_000L, stats.dailyListenedMs["2026-10-08"])
  }
}
