package voice.features.settings.profile

import org.junit.Assert.assertEquals
import org.junit.Test
import voice.core.data.ReaderProfile
import voice.core.data.listening.ListeningStats

class ProfileViewStateTest {
  @Test
  fun `level progress follows five awarded books per level`() {
    assertEquals(0, state(0).level)
    assertEquals(0f, state(0).barFraction)
    assertEquals(0, state(4).level)
    assertEquals(4, state(4).booksIntoLevel)
    assertEquals(0.8f, state(4).barFraction)
    assertEquals(1, state(5).level)
    assertEquals(0, state(5).booksIntoLevel)
    assertEquals(0f, state(5).barFraction)
    assertEquals(1, state(6).level)
    assertEquals(1, state(6).booksIntoLevel)
    assertEquals(2, state(10).level)
    assertEquals(0, state(10).booksIntoLevel)
  }

  @Test
  fun `hours listened come from the durable aggregate`() {
    val stats = ListeningStats(lifetimeListenedMs = 3 * 60 * 60 * 1_000L + 30 * 60 * 1_000L)

    assertEquals(4L, ReaderProfile().toViewState(stats).listenedHours)
  }

  @Test
  fun `awarded books are counted without duplicates`() {
    val stats = ListeningStats(awardedBookIds = setOf("a", "b", "b"))

    assertEquals(2, ReaderProfile().toViewState(stats).completedBooks)
  }

  private fun state(books: Int): ProfileViewState =
    ReaderProfile().toViewState(
      ListeningStats(awardedBookIds = (1..books).map { it.toString() }.toSet()),
    )
}
