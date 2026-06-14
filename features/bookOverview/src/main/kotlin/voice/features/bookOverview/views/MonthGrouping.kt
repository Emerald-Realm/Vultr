package voice.features.bookOverview.views

import androidx.compose.runtime.State
import voice.core.data.BookId
import voice.features.bookOverview.overview.BookOverviewItemViewState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val zone = ZoneId.systemDefault()
private val monthFormatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())

/** Month label such as "June" (current year) or "June 2024" (other years). */
internal fun monthLabel(item: BookOverviewItemViewState): String {
  val date = item.addedAt.atZone(zone).toLocalDate()
  val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    .replaceFirstChar { it.titlecase(Locale.getDefault()) }
  val currentYear = java.time.LocalDate.now(zone).year
  return if (date.year == currentYear) month else "$month ${date.year}"
}

/**
 * Groups books by the month they were added, most-recent month first. Within a month the
 * most recently added book comes first.
 */
internal fun groupBooksByMonth(
  entries: List<Pair<BookId, State<BookOverviewItemViewState>>>,
): List<Pair<String, List<Pair<BookId, State<BookOverviewItemViewState>>>>> {
  return entries
    .sortedByDescending { it.second.value.addedAt }
    .groupBy { monthLabel(it.second.value) }
    .toList()
}
