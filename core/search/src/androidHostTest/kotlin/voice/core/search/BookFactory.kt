package voice.core.search

import voice.core.data.Book
import voice.core.data.BookContent
import voice.core.data.BookId
import voice.core.data.Chapter
import voice.core.data.ChapterId
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

fun book(
  chapters: List<Chapter> = listOf(chapter(), chapter()),
  time: Long = 42,
  currentChapter: ChapterId = chapters.first().id,
  name: String = Uuid.random().toString(),
  author: String? = Uuid.random().toString(),
  genre: String? = "Genre",
  narrator: String? = "Narrator",
  series: String? = null,
  part: String? = null,
): Book {
  return Book(
    content = BookContent(
      author = author,
      name = name,
      positionInChapter = time,
      playbackSpeed = 1F,
      addedAt = Instant.fromEpochMilliseconds(0L),
      chapters = chapters.map { it.id },
      cover = null,
      currentChapter = currentChapter,
      isActive = true,
      lastPlayedAt = Instant.fromEpochMilliseconds(0L),
      skipSilence = false,
      id = BookId(Uuid.random().toString()),
      gain = 0F,
      genre = genre,
      narrator = narrator,
      series = series,
      part = part,
      description = null,
    ),
    chapters = chapters,
  )
}

fun chapter(
  duration: Long = 10000,
  id: ChapterId = ChapterId(Uuid.random().toString()),
): Chapter {
  return Chapter(
    id = id,
    name = Uuid.random().toString(),
    duration = duration,
    fileLastModified = Instant.fromEpochMilliseconds(0L),
    markData = emptyList(),
  )
}
