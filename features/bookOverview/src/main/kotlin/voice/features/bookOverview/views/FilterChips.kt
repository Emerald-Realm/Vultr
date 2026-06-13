package voice.features.bookOverview.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voice.features.bookOverview.overview.BookOverviewCategory

internal val bookFilters = listOf(
  BookOverviewCategory.NOT_STARTED to "Not started",
  BookOverviewCategory.CURRENT to "In Progress",
  BookOverviewCategory.FINISHED to "Completed",
)

@Composable
internal fun FilterChips(
  selected: BookOverviewCategory,
  onSelect: (BookOverviewCategory) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    bookFilters.forEach { (category, label) ->
      FilterChip(
        label = label,
        selected = category == selected,
        onClick = { onSelect(category) },
      )
    }
  }
}

@Composable
private fun FilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(percent = 50),
    color = if (selected) {
      MaterialTheme.colorScheme.inverseSurface
    } else {
      MaterialTheme.colorScheme.surfaceVariant
    },
    contentColor = if (selected) {
      MaterialTheme.colorScheme.inverseOnSurface
    } else {
      MaterialTheme.colorScheme.onSurfaceVariant
    },
  ) {
    Text(
      text = label,
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
      style = MaterialTheme.typography.labelLarge,
    )
  }
}
