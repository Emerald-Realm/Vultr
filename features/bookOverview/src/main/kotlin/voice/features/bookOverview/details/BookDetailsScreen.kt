package voice.features.bookOverview.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.features.bookOverview.views.MiniPlayer
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.ui.R as UiR

@ContributesTo(AppScope::class)
interface BookDetailsGraph {
  val bookDetailsViewModelFactory: BookDetailsViewModel.Factory
}

@ContributesTo(AppScope::class)
interface BookDetailsProvider {

  @Provides
  @IntoSet
  fun bookDetailsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookDetails> { key ->
    NavEntry(key) {
      BookDetailsScreen(key.bookId)
    }
  }
}

@Composable
fun BookDetailsScreen(bookId: voice.core.data.BookId) {
  val viewModel = retain(bookId.value) {
    rootGraphAs<BookDetailsGraph>().bookDetailsViewModelFactory.create(bookId)
  }
  val viewState = viewModel.viewState() ?: return
  BookDetailsScreen(
    viewState = viewState,
    onBackClick = viewModel::onBackClick,
    onPlayClick = viewModel::onPlayClick,
    onChapterClick = viewModel::onChapterClick,
    onMiniPlayerClick = viewModel::onMiniPlayerClick,
    onMiniPlayerPlayClick = viewModel::onMiniPlayerPlayClick,
    onEditClick = viewModel::onEditClick,
  )
  val editForm = viewModel.editForm.value
  if (editForm != null) {
    EditBookSheet(
      form = editForm,
      onDismiss = viewModel::onDismissEdit,
      onSave = viewModel::saveEdit,
      onPickCover = viewModel::onPickCover,
      onDownloadCover = viewModel::onDownloadCover,
    )
  }
}

@Composable
internal fun BookDetailsScreen(
  viewState: BookDetailsViewState,
  onBackClick: () -> Unit,
  onPlayClick: () -> Unit,
  onChapterClick: (BookDetailsViewState.ChapterViewState) -> Unit,
  onMiniPlayerClick: (voice.core.data.BookId) -> Unit,
  onMiniPlayerPlayClick: () -> Unit,
  onEditClick: () -> Unit = {},
) {
  Scaffold(
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBackClick) {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_arrow_left),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
        }
        IconButton(onClick = onEditClick) {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_dots),
            contentDescription = "Edit book",
            modifier = Modifier.size(24.dp),
          )
        }
      }
    },
    bottomBar = {
      viewState.miniPlayer?.let {
        MiniPlayer(
          viewState = it,
          onClick = { onMiniPlayerClick(it.id) },
          onPlayClick = onMiniPlayerPlayClick,
        )
      }
    },
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      item {
        BookCoverArt(
          cover = viewState.cover,
          onPlayClick = onPlayClick,
        )
      }
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = viewState.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
          viewState.author?.let {
            Text(
              text = it,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
      item {
        StatsRow(viewState)
      }
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          LinearProgressIndicator(
            progress = { viewState.progress },
            modifier = Modifier.weight(1F),
            drawStopIndicator = {},
          )
          Text(
            text = viewState.remainingTime,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
      if (viewState.description.isNotBlank()) {
        item {
          ExpandableDescription(viewState.description)
        }
      }
      item {
        Text("Chapters", style = MaterialTheme.typography.titleLarge)
      }
      items(viewState.chapters) { chapter ->
        val primary = MaterialTheme.colorScheme.primary
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .then(if (chapter.isCompleted) Modifier.alpha(0.6f) else Modifier)
            .clickable { onChapterClick(chapter) },
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Text(
              text = chapter.number.toString(),
              modifier = Modifier.width(28.dp),
              style = MaterialTheme.typography.labelMedium,
              color = if (chapter.isCurrent) primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = chapter.title,
              modifier = Modifier.weight(1F),
              style = MaterialTheme.typography.titleMedium,
              color = if (chapter.isCurrent) primary else MaterialTheme.colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = chapter.time,
              modifier = Modifier.padding(start = 8.dp),
              style = MaterialTheme.typography.labelMedium,
              color = if (chapter.isCurrent) primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(Modifier.height(14.dp))
          HorizontalDivider()
        }
      }
    }
  }
}

@Composable
private fun ExpandableDescription(description: String) {
  var expanded by remember { mutableStateOf(false) }
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = description,
      style = MaterialTheme.typography.bodyLarge,
      maxLines = if (expanded) Int.MAX_VALUE else 3,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = if (expanded) "Read less" else "Read more",
      modifier = Modifier
        .padding(top = 4.dp)
        .clickable { expanded = !expanded },
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun StatsRow(
  viewState: BookDetailsViewState,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    StatItem(painter = painterResource(UiR.drawable.ic_mage_clock), text = viewState.durationText)
    StatItem(painter = painterResource(UiR.drawable.ic_mage_book), text = "${viewState.chapterCount} chapters")
    viewState.year?.let { year ->
      StatItem(painter = painterResource(UiR.drawable.ic_mage_calendar), text = year.toString())
    }
  }
}

@Composable
private fun StatItem(
  painter: Painter,
  text: String,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      painter = painter,
      contentDescription = null,
      modifier = Modifier.size(18.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.width(6.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
