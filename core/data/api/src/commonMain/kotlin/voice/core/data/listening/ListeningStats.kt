package voice.core.data.listening

import kotlinx.serialization.Serializable
import voice.core.data.BookId
import voice.core.data.ChapterId

/**
 * Durable listening aggregate. It deliberately survives book and history deletion so that earned
 * levels and reminder history cannot be taken away again.
 */
@Serializable
public data class ListeningStats(
  val lifetimeListenedMs: Long = 0L,
  val dailyListenedMs: Map<String, Long> = emptyMap(),
  val playedRanges: Map<String, Map<String, List<PlayedRange>>> = emptyMap(),
  val awardedBookIds: Set<String> = emptySet(),
  val historySeeded: Boolean = false,
)

@Serializable
public data class PlayedRange(
  val startMs: Long,
  val endMs: Long,
) {
  init {
    require(endMs >= startMs) { "Invalid played range: $startMs..$endMs" }
  }
}

public data class ChapterDuration(
  val id: ChapterId,
  val durationMs: Long,
)

public fun ListeningStats.listenedMs(
  bookId: BookId,
  chapters: List<ChapterDuration>,
): Long {
  val rangesByChapter = playedRanges[bookId.value] ?: return 0L
  return chapters.sumOf { chapter ->
    rangesByChapter[chapter.id.value]
      ?.unionDuration(chapter.durationMs)
      ?: 0L
  }
}

public fun ListeningStats.isBookComplete(
  bookId: BookId,
  chapters: List<ChapterDuration>,
): Boolean {
  val totalDuration = chapters.sumOf { it.durationMs }
  if (totalDuration <= 0L) return false
  return listenedMs(bookId, chapters) * 100 >= totalDuration * COMPLETION_PERCENT
}

public fun ListeningStats.awardBookIfComplete(
  bookId: BookId,
  chapters: List<ChapterDuration>,
): ListeningStats {
  if (bookId.value in awardedBookIds || !isBookComplete(bookId, chapters)) return this
  return copy(
    awardedBookIds = awardedBookIds + bookId.value,
    playedRanges = playedRanges - bookId.value,
  )
}

public fun ListeningStats.addListened(
  elapsedMs: Long,
  byDay: Map<String, Long>,
): ListeningStats {
  if (elapsedMs <= 0L) return this
  val mergedDays = dailyListenedMs.toMutableMap()
  byDay.forEach { (day, listened) ->
    if (listened > 0L) {
      mergedDays[day] = (mergedDays[day] ?: 0L) + listened
    }
  }
  return copy(
    lifetimeListenedMs = lifetimeListenedMs + elapsedMs,
    dailyListenedMs = mergedDays.pruneOlderThan(MAX_STORED_DAYS),
  )
}

public fun ListeningStats.addPlayedRange(
  bookId: BookId,
  chapterId: ChapterId,
  startMs: Long,
  endMs: Long,
): ListeningStats {
  if (endMs <= startMs || startMs < 0L) return this
  val book = playedRanges[bookId.value].orEmpty()
  val chapterRanges = book[chapterId.value].orEmpty()
  val merged = chapterRanges.merge(PlayedRange(startMs, endMs), MAX_RANGES_PER_CHAPTER)
  return copy(
    playedRanges = playedRanges + (bookId.value to (book + (chapterId.value to merged))),
  )
}

public fun ListeningStats.listenedMsInRatingWindow(
  year: Int,
  month: Int,
): Long =
  (RATING_WINDOW_FIRST_DAY..RATING_WINDOW_LAST_DAY).sumOf { day ->
    // Zero-padded ISO date without pulling in a date library for a simple key lookup.
    val key =
      buildString {
        append(year)
        append('-')
        if (month < 10) append('0')
        append(month)
        append('-')
        if (day < 10) append('0')
        append(day)
      }
    dailyListenedMs[key] ?: 0L
  }

public fun List<PlayedRange>.unionDuration(limitMs: Long): Long {
  if (limitMs <= 0L) return 0L
  var total = 0L
  forEach { range ->
    val start = range.startMs.coerceIn(0L, limitMs)
    val end = range.endMs.coerceIn(0L, limitMs)
    if (end > start) total += end - start
  }
  return total
}

public fun List<PlayedRange>.merge(
  newRange: PlayedRange,
  maxRanges: Int = Int.MAX_VALUE,
): List<PlayedRange> {
  val ranges = (this + newRange).sortedBy { it.startMs }
  val merged = mutableListOf<PlayedRange>()
  ranges.forEach { range ->
    val previous = merged.lastOrNull()
    if (previous != null && range.startMs <= previous.endMs) {
      merged[merged.lastIndex] = previous.copy(endMs = maxOf(previous.endMs, range.endMs))
    } else {
      merged += range
    }
  }
  return merged.compact(maxRanges)
}

private fun List<PlayedRange>.compact(maxRanges: Int): List<PlayedRange> {
  if (size <= maxRanges) return this
  val ranges = toMutableList()
  while (ranges.size > maxRanges) {
    var smallestGapIndex = 0
    var smallestGap = Long.MAX_VALUE
    for (index in 0 until ranges.lastIndex) {
      val gap = ranges[index + 1].startMs - ranges[index].endMs
      if (gap < smallestGap) {
        smallestGap = gap
        smallestGapIndex = index
      }
    }
    val current = ranges[smallestGapIndex]
    val next = ranges[smallestGapIndex + 1]
    ranges[smallestGapIndex] = current.copy(endMs = maxOf(current.endMs, next.endMs))
    ranges.removeAt(smallestGapIndex + 1)
  }
  return ranges
}

private fun Map<String, Long>.pruneOlderThan(days: Int): Map<String, Long> {
  if (size <= days) return this
  return entries
    .sortedByDescending { it.key }
    .take(days)
    .associate { it.key to it.value }
}

public const val COMPLETION_PERCENT: Int = 95
public const val RATING_WINDOW_FIRST_DAY: Int = 8
public const val RATING_WINDOW_LAST_DAY: Int = 14
public const val RATING_WINDOW_LISTENED_MS: Long = 3_600_000L

internal const val MAX_RANGES_PER_CHAPTER: Int = 256
private const val MAX_STORED_DAYS = 400
