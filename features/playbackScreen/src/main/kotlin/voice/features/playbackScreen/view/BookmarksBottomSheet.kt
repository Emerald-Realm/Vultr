package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import voice.core.data.Bookmark
import voice.core.ui.RavenTheme
import voice.core.ui.R as UiR

data class BookmarkChapterGroup(
  val chapterName: String,
  val items: List<BookmarkItemUi>,
)

data class BookmarkItemUi(
  val id: Bookmark.Id,
  val title: String,
  val timeAndDate: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookmarksBottomSheet(
  groups: List<BookmarkChapterGroup>,
  onDismiss: () -> Unit,
  onAddBookmark: () -> Unit,
  onExportBookmarks: () -> Unit,
  onEditBookmark: (Bookmark.Id) -> Unit,
  onDeleteBookmark: (Bookmark.Id) -> Unit,
  onBookmarkClick: (Bookmark.Id) -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    containerColor = RavenTheme.colors.bgMain,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(bottom = 16.dp),
    ) {
      Text(
        text = "Bookmarks",
        modifier = Modifier.fillMaxWidth(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
        color = RavenTheme.colors.title,
      )
      Spacer(Modifier.height(24.dp))
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f, fill = false)
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        groups.forEach { group ->
          item(key = "header-${group.chapterName}") {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = group.chapterName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.06).sp,
                color = RavenTheme.colors.title,
              )
              Text(
                text = "${group.items.size} notes",
                fontSize = 12.sp,
                letterSpacing = (-0.06).sp,
                color = RavenTheme.colors.caption,
              )
            }
          }
          items(
            items = group.items,
            key = { it.id.value.toString() },
          ) { bookmark ->
            BookmarkRow(
              bookmark = bookmark,
              onClick = { onBookmarkClick(bookmark.id) },
              onEdit = { onEditBookmark(bookmark.id) },
              onDelete = { onDeleteBookmark(bookmark.id) },
            )
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      Row(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Surface(
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable(onClick = onAddBookmark),
          shape = RoundedCornerShape(12.dp),
          color = Color.Transparent,
          border = androidx.compose.foundation.BorderStroke(1.dp, RavenTheme.colors.borderStrong),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              painter = painterResource(UiR.drawable.ic_mage_bookmark_plus),
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = RavenTheme.colors.primary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
              text = "Add Bookmark",
              fontSize = 14.sp,
              letterSpacing = (-0.07).sp,
              color = RavenTheme.colors.subTitle,
            )
          }
        }
        Surface(
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable(onClick = onExportBookmarks),
          shape = RoundedCornerShape(12.dp),
          color = RavenTheme.colors.primary,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Export as .txt",
              color = Color.White,
              fontWeight = FontWeight.Medium,
              fontSize = 16.sp,
              letterSpacing = (-0.08).sp,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BookmarkRow(
  bookmark: BookmarkItemUi,
  onClick: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  var showPopup by remember { mutableStateOf(false) }
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(6.dp),
    color = RavenTheme.colors.bgTertiary,
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        painter = painterResource(UiR.drawable.ic_mage_bookmark_fill),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = RavenTheme.colors.primary,
      )
      Spacer(Modifier.width(8.dp))
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = bookmark.title.ifEmpty { "Bookmark" },
          fontSize = 12.sp,
          letterSpacing = (-0.06).sp,
          color = RavenTheme.colors.subTitle,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = bookmark.timeAndDate,
          fontSize = 12.sp,
          letterSpacing = (-0.06).sp,
          color = RavenTheme.colors.caption,
        )
      }
      Box {
        Icon(
          painter = painterResource(UiR.drawable.ic_mage_dots),
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .clickable { showPopup = true },
          tint = RavenTheme.colors.subTitle,
        )
        if (showPopup) {
          Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(0, 80),
            onDismissRequest = { showPopup = false },
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = RavenTheme.colors.bgModal,
              shadowElevation = 4.dp,
              border = androidx.compose.foundation.BorderStroke(1.dp, RavenTheme.colors.borderAvg),
            ) {
              Column(
                modifier = Modifier
                  .width(160.dp)
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                Text(
                  text = "Edit Bookmark",
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      showPopup = false
                      onEdit()
                    },
                  fontSize = 14.sp,
                  letterSpacing = (-0.07).sp,
                  color = RavenTheme.colors.subTitle,
                )
                Text(
                  text = "Delete Bookmark",
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      showPopup = false
                      onDelete()
                    },
                  fontSize = 14.sp,
                  letterSpacing = (-0.07).sp,
                  color = RavenTheme.colors.subTitle,
                )
              }
            }
          }
        }
      }
    }
  }
}
