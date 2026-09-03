package voice.core.data

import io.kotest.matchers.shouldBe
import org.junit.Test

class BookProgressCategoryTest {

  @Test
  fun `book at the start is not started`() {
    book(time = 0).progressCategory shouldBe BookProgressCategory.NOT_STARTED
  }

  @Test
  fun `book in the middle is current`() {
    book(time = 42).progressCategory shouldBe BookProgressCategory.CURRENT
  }

  @Test
  fun `book within five seconds of the end is finished`() {
    val chapters = listOf(chapter(duration = 10_000), chapter(duration = 10_000))
    book(
      chapters = chapters,
      currentChapter = chapters.last().id,
      time = 6_000,
    ).progressCategory shouldBe BookProgressCategory.FINISHED
  }
}
