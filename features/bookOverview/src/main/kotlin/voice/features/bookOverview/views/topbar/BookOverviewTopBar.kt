package voice.features.bookOverview.views.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import voice.core.data.BookId
import voice.core.ui.VoiceTheme
import voice.features.bookOverview.overview.BookOverviewLayoutMode
import voice.features.bookOverview.overview.BookOverviewViewState
import voice.features.bookOverview.search.BookSearchViewState
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BookOverviewTopBar(
  viewState: BookOverviewViewState,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onActiveChange: (Boolean) -> Unit,
  onQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
) {
  Column {
    if (viewState.searchActive) {
      BookOverviewSearchBar(
        horizontalPadding = 0.dp,
        onQueryChange = onQueryChange,
        onActiveChange = onActiveChange,
        onBookFolderClick = onBookFolderClick,
        onSettingsClick = onSettingsClick,
        onSearchBookClick = onSearchBookClick,
        searchActive = true,
        showAddBookHint = viewState.showAddBookHint,
        showFolderPickerIcon = viewState.showFolderPickerIcon,
        searchViewState = viewState.searchViewState,
      )
    } else {
      MyLibraryHeader(
        onLibraryClick = onBookFolderClick,
        onSearchClick = { onActiveChange(true) },
        onSettingsClick = onSettingsClick,
      )
    }
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewState.isLoading) {
      if (viewState.isLoading) {
        delay(3.seconds)
      }
      showLoading = viewState.isLoading
    }
    if (showLoading) {
      LinearProgressIndicator(
        Modifier
          .padding(top = 12.dp)
          .fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun MyLibraryHeader(
  onLibraryClick: () -> Unit,
  onSearchClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onLibraryClick)
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
        contentDescription = null,
      )
      Spacer(Modifier.width(10.dp))
      Text(
        text = "My Library",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
    }
    Spacer(Modifier.weight(1F))
    IconButton(onClick = onSearchClick) {
      Icon(imageVector = Icons.Filled.Search, contentDescription = null)
    }
    IconButton(onClick = onSettingsClick) {
      Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
    }
  }
}

@Composable
@Preview
private fun BookOverviewTopBarPreview() {
  VoiceTheme {
    BookOverviewTopBar(
      viewState = BookOverviewViewState(
        books = emptyMap(),
        layoutMode = BookOverviewLayoutMode.List,
        playButtonState = BookOverviewViewState.PlayButtonState.Paused,
        showAddBookHint = true,
        showSearchIcon = true,
        isLoading = true,
        searchActive = false,
        searchViewState = BookSearchViewState.EmptySearch(
          suggestedAuthors = listOf(),
          recentQueries = listOf(),
          query = "",
        ),
        showStoragePermissionBugCard = false,
        showFolderPickerIcon = true,
        miniPlayer = null,
      ),
      onBookFolderClick = {},
      onSettingsClick = {},
      onActiveChange = {},
      onQueryChange = {},
      onSearchBookClick = {},
    )
  }
}
