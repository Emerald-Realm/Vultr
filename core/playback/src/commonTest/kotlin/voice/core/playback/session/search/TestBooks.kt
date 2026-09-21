package voice.core.playback.session.search

import kotlin.uuid.Uuid
import kotlinx.datetime.Instant
import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId

fun book(
  chapters: List<Chapter>,
  id: BookId = BookId(Uuid.random().toString()),
  positionInChapter: Long = 0,
): Book {
  return Book(
    content = BookContent(
      author = Uuid.random().toString(),
      name = Uuid.random().toString(),
      positionInChapter = positionInChapter,
      playbackSpeed = 1F,
      addedAt = Instant.fromEpochMilliseconds(0L),
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = chapters.first().id,
      isActive = true,
      lastPlayedAt = Instant.fromEpochMilliseconds(0L),
      skipSilence = false,
      id = id,
      gain = 0F,
      genre = null,
      narrator = null,
      series = null,
      part = null,
      description = null,
    ),
    chapters = chapters,
  )
}

fun chapter(): Chapter {
  return Chapter(
    id = ChapterId(Uuid.random().toString()),
    name = Uuid.random().toString(),
    duration = 10000,
    fileLastModified = Instant.fromEpochMilliseconds(0L),
    markData = emptyList(),
  )
}
