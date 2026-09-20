package voice.app

import android.content.Context
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import dev.zacsweers.metro.Inject
import io.kotest.matchers.longs.shouldBeGreaterThan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import voice.core.common.rootGraphAs
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId
import voice.core.data.MarkData
import voice.core.data.repo.BookContentRepo
import voice.core.data.repo.ChapterRepo
import voice.core.data.store.CurrentBookStore
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlayStateManager
import voice.core.sleeptimer.SleepTimer
import voice.core.sleeptimer.SleepTimerMode
import voice.core.sleeptimer.SleepTimerState
import java.io.File
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid
import kotlin.time.Duration.Companion.seconds

class SleepTimerIntegrationTest {

  @Inject
  lateinit var playerController: PlayerController

  @field:[Inject CurrentBookStore]
  lateinit var currentBookStore: DataStore<BookId?>

  @Inject
  lateinit var bookContentRepo: BookContentRepo

  @Inject
  lateinit var chapterRepo: ChapterRepo

  @Inject
  lateinit var sleepTimer: SleepTimer

  @Inject
  lateinit var playStateManager: PlayStateManager

  @Test
  fun testWithTimedMode() = runTest {
    rootGraphAs<TestGraph>().inject(this@SleepTimerIntegrationTest)

    val bookId = prepareTestBook()

    // play the book and wait for it to start
    playerController.play()
    playStateManager.flow.first { it == PlayStateManager.PlayState.Playing }

    sleepTimer.enable(SleepTimerMode.TimedWithDuration(3.seconds))

    // wait for the sleep timer to trigger
    sleepTimer.state.first { it == SleepTimerState.Disabled }
    playStateManager.flow.first { it == PlayStateManager.PlayState.Paused }

    bookContentRepo.get(bookId)!!.positionInChapter.shouldBeGreaterThan(0)
  }

  @Test
  fun testWithEndOfChapterMode() = runTest {
    rootGraphAs<TestGraph>().inject(this@SleepTimerIntegrationTest)

    val bookId = prepareTestBook()

    // play the book and wait for it to start
    playerController.play()
    playStateManager.flow.first { it == PlayStateManager.PlayState.Playing }

    sleepTimer.enable(SleepTimerMode.EndOfChapter)

    // wait for the sleep timer to trigger
    playStateManager.flow.first { it == PlayStateManager.PlayState.Paused }
    sleepTimer.state.first { it == SleepTimerState.Disabled }

    // suspend until the position is updated to the end of the chapter
    bookContentRepo.flow(bookId)
      .first { it!!.positionInChapter == 1000L }
  }

  private suspend fun prepareTestBook(): BookId {
    val audioFile = copyTestAudioFile()

    val bookId = BookId(Uuid.random().toString())
    val chapterId = ChapterId(audioFile.toUri())

    val chapter = Chapter(
      id = chapterId,
      duration = 119210,
      name = "Test Chapter",
      fileLastModified = Instant.fromEpochMilliseconds(0L),
      markData = listOf(
        MarkData(startMs = 0, name = "Mark 1"),
        MarkData(startMs = 1000, name = "Mark 2"),
      ),
    )

    val bookContent = BookContent(
      id = bookId,
      playbackSpeed = 1.0f,
      skipSilence = false,
      isActive = true,
      lastPlayedAt = Clock.System.now(),
      author = "Test Author",
      name = "Test Audio Book",
      addedAt = Clock.System.now(),
      chapters = listOf(chapterId),
      currentChapter = chapterId,
      positionInChapter = 0L,
      cover = null,
      gain = 0f,
      genre = null,
      narrator = null,
      series = null,
      part = null,
      description = null,
    )

    bookContentRepo.put(bookContent)
    chapterRepo.put(chapter)

    currentBookStore.updateData { bookId }

    return bookId
  }

  private fun copyTestAudioFile(): File {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val outputFile = File(context.filesDir, "auphonic_chapters_demo.m4a")
    InstrumentationRegistry.getInstrumentation().context.assets
      .open("auphonic_chapters_demo.m4a").use { inputStream ->
        outputFile.outputStream().use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }
    return outputFile
  }
}
