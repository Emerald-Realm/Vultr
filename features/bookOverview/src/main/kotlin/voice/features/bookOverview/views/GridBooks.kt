package voice.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.data.BookId
import voice.features.bookOverview.overview.BookOverviewCategory
import voice.features.bookOverview.overview.BookOverviewItemViewState
import androidx.compose.foundation.shape.RoundedCornerShape
import voice.core.ui.R as UiR

@Composable
internal fun GridBooks(
  books: Map<BookOverviewCategory, Map<BookId, State<BookOverviewItemViewState>>>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  showPermissionBugCard: Boolean,
  onPermissionBugCardClick: () -> Unit,
  showHeaders: Boolean = true,
) {
  val cellCount = gridColumnCount()
  LazyVerticalGrid(
    columns = GridCells.Fixed(cellCount),
    verticalArrangement = Arrangement.spacedBy(28.dp),
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 24.dp),
  ) {
    if (showPermissionBugCard) {
      item(
        span = { GridItemSpan(maxLineSpan) },
      ) {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    books.forEach { (category, books) ->
      if (books.isEmpty()) return@forEach
      if (showHeaders) {
        item(
          span = { GridItemSpan(maxLineSpan) },
          key = category,
          contentType = "header",
        ) {
          Header(
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            category = category,
          )
        }
      }
      items(
        items = books.toList(),
        key = { (bookId, _) -> bookId.value },
        contentType = { "item" },
      ) { (_, bookState) ->
        GridBook(
          book = bookState.value,
          onBookClick = onBookClick,
          onBookLongClick = onBookLongClick,
        )
      }
      item(
        span = { GridItemSpan(maxLineSpan) },
      ) {
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
      }
    }
  }
}

@Composable
internal fun GridBook(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = { onBookClick(book.id) },
        onLongClick = { onBookLongClick(book.id) },
      ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(2.dp)),
      contentAlignment = Alignment.Center,
    ) {
      AsyncImage(
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        model = book.cover?.file,
        placeholder = painterResource(id = UiR.drawable.album_art),
        error = painterResource(id = UiR.drawable.album_art),
        contentDescription = null,
      )
    }

      Spacer(Modifier.height(4.dp))

      Text(
        text = book.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      book.author?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (book.progress > 0f) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          LinearProgressIndicator(
            progress = { book.progress },
            modifier = Modifier
              .weight(1F)
              .height(4.dp)
              .clip(MaterialTheme.shapes.small),
            drawStopIndicator = {},
          )
          Text(
            text = book.remainingTime,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
          )
        }
      }
  }
}

@Composable
internal fun gridColumnCount(): Int = 3

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithProgress() {
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0.66f), {}, {})
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithoutProgress() {
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0f), {}, {})
}
