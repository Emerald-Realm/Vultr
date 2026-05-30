package voice.features.bookOverview.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import coil.compose.AsyncImage
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
  )
}

@Composable
internal fun BookDetailsScreen(
  viewState: BookDetailsViewState,
  onBackClick: () -> Unit,
  onPlayClick: () -> Unit,
  onChapterClick: (BookDetailsViewState.ChapterViewState) -> Unit,
  onMiniPlayerClick: (voice.core.data.BookId) -> Unit,
  onMiniPlayerPlayClick: () -> Unit,
) {
  Scaffold(
    topBar = {
      IconButton(
        modifier = Modifier.statusBarsPadding(),
        onClick = onBackClick,
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
        AsyncImage(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1F)
            .clip(RoundedCornerShape(2.dp)),
          model = viewState.cover?.file,
          placeholder = painterResource(id = UiR.drawable.album_art),
          error = painterResource(id = UiR.drawable.album_art),
          contentScale = ContentScale.Crop,
          contentDescription = null,
        )
      }
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1F)) {
            Text(viewState.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            viewState.author?.let {
              Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          FilledIconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
          }
        }
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
          Text(viewState.description, style = MaterialTheme.typography.bodyLarge)
        }
      }
      item {
        Text("Chapters", style = MaterialTheme.typography.titleLarge)
      }
      items(viewState.chapters) { chapter ->
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .then(if (chapter.isCompleted) Modifier.alpha(0.6f) else Modifier)
            .clickable { onChapterClick(chapter) },
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = chapter.title,
              modifier = Modifier.weight(1F),
              style = MaterialTheme.typography.titleMedium,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            Text(chapter.time, style = MaterialTheme.typography.labelMedium)
          }
          Spacer(Modifier.height(14.dp))
          HorizontalDivider()
        }
      }
    }
  }
}
