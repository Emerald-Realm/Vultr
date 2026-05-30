package voice.features.bookmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.data.ListeningSession
import voice.features.bookmark.dialogs.AddBookmarkDialog
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import java.util.UUID
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface Graph {
  val bookmarkViewModelFactory: BookmarkViewModel.Factory
}

@ContributesTo(AppScope::class)
interface BookmarkProvider {

  @Provides
  @IntoSet
  fun bookmarkNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Bookmarks> { key ->
    NavEntry(key) {
      BookmarkScreen(bookId = key.bookId)
    }
  }
}

@Composable
fun BookmarkScreen(bookId: BookId) {
  val viewModel = retain(bookId.value) {
    rootGraphAs<Graph>().bookmarkViewModelFactory.create(bookId)
  }
  val viewState = viewModel.viewState()
  BookmarkScreen(
    viewState = viewState,
    onClose = viewModel::closeScreen,
    onAdd = viewModel::onAddClick,
    onDelete = viewModel::deleteEntry,
    onClick = viewModel::selectEntry,
    onNewBookmarkNameChoose = viewModel::addBookmark,
    onCloseDialog = viewModel::closeDialog,
  )
}

@Composable
internal fun BookmarkScreen(
  viewState: BookmarkViewState,
  onClose: () -> Unit,
  onAdd: () -> Unit,
  onDelete: (ListeningSession.Id) -> Unit,
  onClick: (ListeningSession.Id) -> Unit,
  onCloseDialog: () -> Unit,
  onNewBookmarkNameChoose: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    modifier = modifier,
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        title = { Text(text = "History") },
        navigationIcon = {
          IconButton(onClick = onClose) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = stringResource(id = StringsR.string.close),
            )
          }
        },
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAdd,
        content = {
          Icon(Icons.Default.Add, contentDescription = stringResource(id = StringsR.string.add))
        },
      )
    },
  ) { paddingValues ->
    val lazyListState = rememberLazyListState()
    LazyColumn(
      state = lazyListState,
      contentPadding = paddingValues,
    ) {
      viewState.groups.forEach { group ->
        item(key = "header-${group.label}") {
          SectionHeader(
            label = group.label,
            summary = group.summary,
          )
        }
        items(
          items = group.items,
          key = { it.id.value.toString() },
        ) { item ->
          HistoryItem(
            modifier = Modifier.animateItem(),
            item = item,
            onDelete = onDelete,
            onClick = onClick,
          )
        }
      }
      item {
        Spacer(Modifier.size(88.dp))
      }
    }
  }

  when (viewState.dialogViewState) {
    BookmarkDialogViewState.AddBookmark -> {
      AddBookmarkDialog(
        onDismissRequest = onCloseDialog,
        onBookmarkNameChoose = onNewBookmarkNameChoose,
      )
    }
    BookmarkDialogViewState.None -> {
    }
  }
}

@Composable
private fun SectionHeader(
  label: String,
  summary: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1F),
    )
    Text(
      text = summary,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
internal fun HistoryItem(
  item: HistoryItemViewState,
  onDelete: (ListeningSession.Id) -> Unit,
  onClick: (ListeningSession.Id) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  SwipeToDismissBox(
    modifier = modifier,
    onDismiss = {
      if (it == SwipeToDismissBoxValue.StartToEnd) {
        onDelete(item.id)
      }
    },
    enableDismissFromEndToStart = false,
    backgroundContent = {
      Box(Modifier.fillMaxSize()) {
        Icon(
          modifier = Modifier
            .padding(start = 16.dp)
            .align(Alignment.CenterStart),
          imageVector = Icons.Outlined.Delete,
          contentDescription = stringResource(id = StringsR.string.delete),
          tint = Color.White,
        )
      }
    },
    state = rememberSwipeToDismissBoxState(),
    content = {
      Surface(
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .fillMaxWidth()
          .clickable {
            onClick(item.id)
          },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
      ) {
        Row(
          modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1F)) {
            Text(
              text = "${item.chapterName} - ${item.timeWithinChapter}",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "${item.action} · ${item.globalBookDuration}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Box {
            IconButton(
              onClick = {
                expanded = !expanded
              },
              content = {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = stringResource(id = StringsR.string.remove),
                )
              },
            )
            DropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
            ) {
              DropdownMenuItem(
                text = { Text(stringResource(id = StringsR.string.remove)) },
                onClick = {
                  expanded = false
                  onDelete(item.id)
                },
              )
            }
          }
        }
      }
    },
  )
}

@Composable
@Preview
private fun HistoryItemPreview() {
  HistoryItem(
    item = HistoryItemViewState(
      id = ListeningSession.Id(UUID.randomUUID()),
      chapterName = "Chapter 4",
      timeWithinChapter = "12:05",
      action = "Paused",
      globalBookDuration = "4:13:03",
    ),
    onDelete = {},
    onClick = {},
  )
}
