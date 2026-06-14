package voice.features.bookOverview.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import voice.core.ui.RavenTheme
import voice.core.ui.R as UiR
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.launch
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.ui.PlayButton
import voice.core.ui.VoiceTheme
import voice.features.bookOverview.bottomSheet.BottomSheetContent
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.deleteBook.DeleteBookDialog
import voice.features.bookOverview.di.BookOverviewGraph
import voice.features.bookOverview.editTitle.EditBookTitleDialog
import voice.features.bookOverview.overview.BookOverviewCategory
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.BookOverviewLayoutMode
import voice.features.bookOverview.overview.BookOverviewViewState
import voice.features.bookOverview.search.BookSearchViewState
import voice.features.bookOverview.views.topbar.BookOverviewTopBar
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import java.time.Instant
import java.util.UUID

@ContributesTo(AppScope::class)
interface BookOverviewProvider {

  @Provides
  @IntoSet
  fun bookOverviewNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookOverview> { key ->
    NavEntry(key) {
      BookOverviewScreen()
    }
  }
}

// Chip / pager order: Not started, In Progress, Completed.
private val categoryOrder = listOf(
  BookOverviewCategory.NOT_STARTED,
  BookOverviewCategory.CURRENT,
  BookOverviewCategory.FINISHED,
)

@Composable
fun BookOverviewScreen(modifier: Modifier = Modifier) {
  val bookGraph = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }
  val bookOverviewViewModel = bookGraph.bookOverviewViewModel
  val editBookTitleViewModel = bookGraph.editBookTitleViewModel
  val bottomSheetViewModel = bookGraph.bottomSheetViewModel
  val deleteBookViewModel = bookGraph.deleteBookViewModel
  val fileCoverViewModel = bookGraph.fileCoverViewModel

  LaunchedEffect(Unit) {
    bookOverviewViewModel.attach()
  }
  val viewState = bookOverviewViewModel.state()

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var feedbackMessage by remember { mutableStateOf<String?>(null) }

  fun bookName(id: BookId?): String? =
    id?.let { bookId -> viewState.books.values.firstNotNullOfOrNull { it[bookId]?.value?.name } }

  fun showFeedback(message: String) {
    feedbackMessage = message
    scope.launch {
      kotlinx.coroutines.delay(2500)
      if (feedbackMessage == message) feedbackMessage = null
    }
  }

  val getContentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        fileCoverViewModel.onImagePicked(uri)
      }
    },
  )

  var showBottomSheet by remember { mutableStateOf(false) }
  Box(modifier = Modifier.fillMaxSize()) {
  BookOverview(
    viewState = viewState,
    snackbarHostState = snackbarHostState,
    onSettingsClick = bookOverviewViewModel::onSettingsClick,
    onBookClick = bookOverviewViewModel::onBookClick,
    onBookLongClick = { bookId ->
      bottomSheetViewModel.bookSelected(bookId)
      showBottomSheet = true
    },
    onBookFolderClick = bookOverviewViewModel::onBookFolderClick,
    onPlayButtonClick = bookOverviewViewModel::playPause,
    onPlayBookClick = bookOverviewViewModel::onPlayBookClick,
    onSearchActiveChange = bookOverviewViewModel::onSearchActiveChange,
    onSearchQueryChange = bookOverviewViewModel::onSearchQueryChange,
    onSearchBookClick = bookOverviewViewModel::onSearchBookClick,
    onPermissionBugCardClick = bookOverviewViewModel::onPermissionBugCardClick,
    onMiniPlayerClick = bookOverviewViewModel::onMiniPlayerClick,
  )
  val deleteBookViewState = deleteBookViewModel.state.value
  if (deleteBookViewState != null) {
    val name = bookName(deleteBookViewState.id)
    DeleteBookDialog(
      viewState = deleteBookViewState,
      onDismiss = deleteBookViewModel::onDismiss,
      onConfirmDeletion = {
        deleteBookViewModel.onConfirmDeletion()
        showFeedback(name?.let { "$it deleted" } ?: "Book deleted")
      },
      onDeleteCheckBoxCheck = deleteBookViewModel::onDeleteCheckBoxCheck,
    )
  }
  val editBookTitleState = editBookTitleViewModel.state.value
  if (editBookTitleState != null) {
    EditBookTitleDialog(
      onDismissEditTitleClick = editBookTitleViewModel::onDismissEditTitle,
      onConfirmEditTitle = {
        editBookTitleViewModel.onConfirmEditTitle()
        showFeedback("Title updated")
      },
      viewState = editBookTitleState,
      onUpdateEditTitle = editBookTitleViewModel::onUpdateEditTitle,
    )
  }

  if (showBottomSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      modifier = modifier,
      sheetState = sheetState,
      content = {
        BottomSheetContent(
          state = bottomSheetViewModel.state.value,
          onItemClick = { item ->
            val name = bookName(bottomSheetViewModel.bookId)
            if (item == BottomSheetItem.FileCover) {
              getContentLauncher.launch("image/*")
            }
            scope.launch {
              sheetState.hide()
              bottomSheetViewModel.onItemClick(item)
              showBottomSheet = false
            }
            feedbackForItem(item, name)?.let(::showFeedback)
          },
        )
      },
      onDismissRequest = {
        showBottomSheet = false
      },
    )
  }
    FeedbackBanner(
      message = feedbackMessage,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Composable
private fun FeedbackBanner(
  message: String?,
  modifier: Modifier = Modifier,
) {
  androidx.compose.animation.AnimatedVisibility(
    visible = message != null,
    modifier = modifier,
    enter = androidx.compose.animation.slideInVertically { -it } + androidx.compose.animation.fadeIn(),
    exit = androidx.compose.animation.slideOutVertically { -it } + androidx.compose.animation.fadeOut(),
  ) {
    androidx.compose.material3.Surface(
      modifier = Modifier
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
      color = RavenTheme.colors.bgModal,
      shadowElevation = 6.dp,
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        androidx.compose.material3.Surface(
          modifier = Modifier.size(24.dp),
          shape = androidx.compose.foundation.shape.CircleShape,
          color = RavenTheme.colors.successBase,
        ) {
          Box(contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
              painter = painterResource(UiR.drawable.ic_mage_check),
              contentDescription = null,
              tint = RavenTheme.colors.white,
              modifier = Modifier.size(16.dp),
            )
          }
        }
        Spacer(Modifier.width(10.dp))
        androidx.compose.material3.Text(
          text = message.orEmpty(),
          fontSize = 14.sp,
          letterSpacing = (-0.07).sp,
          color = RavenTheme.colors.title,
        )
      }
    }
  }
}

@Composable
private fun ScanningBar(modifier: Modifier = Modifier) {
  androidx.compose.material3.Surface(
    modifier = modifier.fillMaxWidth(),
    color = RavenTheme.colors.bgMain,
  ) {
    Column {
      androidx.compose.material3.HorizontalDivider(color = RavenTheme.colors.primaryFaint)
      androidx.compose.material3.Text(
        text = "Scanning",
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 14.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        fontSize = 16.sp,
        letterSpacing = (-0.08).sp,
        color = RavenTheme.colors.primary,
      )
    }
  }
}

private fun feedbackForItem(item: BottomSheetItem, name: String?): String? {
  val prefix = name ?: "Book"
  return when (item) {
    BottomSheetItem.BookCategoryMarkAsCompleted -> "$prefix marked as completed"
    BottomSheetItem.BookCategoryMarkAsNotStarted -> "$prefix marked as not started"
    BottomSheetItem.BookCategoryMarkAsCurrent -> "$prefix marked as in progress"
    else -> null
  }
}

@Composable
internal fun BookOverview(
  viewState: BookOverviewViewState,
  onSettingsClick: () -> Unit,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onBookFolderClick: () -> Unit,
  onPlayButtonClick: () -> Unit,
  onPlayBookClick: (BookId) -> Unit,
  onSearchActiveChange: (Boolean) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
  onPermissionBugCardClick: () -> Unit,
  onMiniPlayerClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val scope = rememberCoroutineScope()
  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      BookOverviewTopBar(
        viewState = viewState,
        onBookFolderClick = onBookFolderClick,
        onSettingsClick = onSettingsClick,
        onActiveChange = onSearchActiveChange,
        onQueryChange = onSearchQueryChange,
        onSearchBookClick = onSearchBookClick,
      )
    },
    bottomBar = {
      Column {
        viewState.miniPlayer?.let { miniPlayer ->
          MiniPlayer(
            viewState = miniPlayer,
            onClick = { onMiniPlayerClick(miniPlayer.id) },
            onPlayClick = onPlayButtonClick,
          )
        }
        if (viewState.isLoading) {
          ScanningBar()
        }
      }
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { contentPadding ->
    Column(
      Modifier
        .padding(contentPadding)
        .consumeWindowInsets(contentPadding),
    ) {
      val initialPage = remember(viewState.books.keys) { defaultFilterIndex(viewState) }
      val pagerState = rememberPagerState(initialPage = initialPage) { categoryOrder.size }
      FilterChips(
        selectedIndex = pagerState.currentPage,
        onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
      )
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
      ) { page ->
        val category = categoryOrder[page]
        val entries = viewState.books[category].orEmpty().toList()
        when (viewState.layoutMode) {
          BookOverviewLayoutMode.List -> {
            ListBooks(
              entries = entries,
              currentBookId = viewState.currentBookId,
              isPlaying = viewState.isPlaying,
              onBookClick = onBookClick,
              onBookLongClick = onBookLongClick,
              onPlayClick = onPlayBookClick,
              onMenuClick = onBookLongClick,
              showPermissionBugCard = viewState.showStoragePermissionBugCard,
              onPermissionBugCardClick = onPermissionBugCardClick,
            )
          }
          BookOverviewLayoutMode.Grid -> {
            GridBooks(
              entries = entries,
              columns = viewState.gridColumns,
              currentBookId = viewState.currentBookId,
              isPlaying = viewState.isPlaying,
              onBookClick = onBookClick,
              onBookLongClick = onBookLongClick,
              onPlayClick = onPlayBookClick,
              showPermissionBugCard = viewState.showStoragePermissionBugCard,
              onPermissionBugCardClick = onPermissionBugCardClick,
            )
          }
        }
      }
    }
  }
}

private fun defaultFilterIndex(viewState: BookOverviewViewState): Int {
  // After a fresh scan books land in "Not started", so prefer that, then In Progress, then Completed.
  val preference = listOf(
    BookOverviewCategory.NOT_STARTED,
    BookOverviewCategory.CURRENT,
    BookOverviewCategory.FINISHED,
  )
  val category = preference.firstOrNull { viewState.books[it]?.isNotEmpty() == true }
    ?: BookOverviewCategory.NOT_STARTED
  return categoryOrder.indexOf(category).coerceAtLeast(0)
}

@Suppress("ktlint:compose:preview-public-check")
@Preview
@Composable
fun BookOverviewPreview(
  @PreviewParameter(BookOverviewPreviewParameterProvider::class)
  viewState: BookOverviewViewState,
) {
  VoiceTheme {
    BookOverview(
      viewState = viewState,
      onSettingsClick = {},
      onBookClick = {},
      onBookLongClick = {},
      onBookFolderClick = {},
      onPlayButtonClick = {},
      onPlayBookClick = {},
      onSearchActiveChange = {},
      onSearchQueryChange = {},
      onSearchBookClick = {},
      onPermissionBugCardClick = {},
      onMiniPlayerClick = {},
    )
  }
}

internal class BookOverviewPreviewParameterProvider : PreviewParameterProvider<BookOverviewViewState> {

  fun book(): BookOverviewItemViewState {
    return BookOverviewItemViewState(
      name = "Book",
      author = "Author",
      cover = null,
      progress = 0.8F,
      id = BookId(UUID.randomUUID().toString()),
      remainingTime = "01:04",
      addedAt = Instant.now(),
    )
  }

  override val values = sequenceOf(
    BookOverviewViewState(
      books = mapOf(
        BookOverviewCategory.CURRENT to buildMap {
          repeat(10) {
            put(
              BookId(UUID.randomUUID().toString()),
              mutableStateOf(book()),
            )
          }
        },
        BookOverviewCategory.FINISHED to buildMap {
          repeat(2) {
            put(
              BookId(UUID.randomUUID().toString()),
              mutableStateOf(book()),
            )
          }
        },
      ),
      layoutMode = BookOverviewLayoutMode.List,
      gridColumns = 2,
      currentBookId = null,
      isPlaying = false,
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = true,
      searchActive = true,
      searchViewState = BookSearchViewState.EmptySearch(
        suggestedAuthors = emptyList(),
        recentQueries = emptyList(),
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      miniPlayer = null,
    ),
  )
}
