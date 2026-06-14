package voice.features.playbackScreen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.data.Bookmark
import voice.core.ui.RavenTheme
import voice.core.ui.R as UiR

data class EditBookmarkState(
  val bookmarkId: Bookmark.Id,
  val chapterInfo: String,
  val currentTitle: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditBookmarkSheet(
  state: EditBookmarkState,
  onDismiss: () -> Unit,
  onSave: (Bookmark.Id, String) -> Unit,
) {
  var text by remember(state.bookmarkId) { mutableStateOf(state.currentTitle) }
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.92f)
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        IconButton(onClick = onDismiss) {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_arrow_left),
            contentDescription = "Back",
            modifier = Modifier.size(24.dp),
          )
        }
        Text(
          text = "Edit Bookmark",
          fontSize = 24.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = (-0.12).sp,
          color = RavenTheme.colors.title,
        )
      }
      Spacer(Modifier.height(24.dp))
      Text(
        text = state.chapterInfo,
        fontSize = 12.sp,
        letterSpacing = (-0.06).sp,
        color = RavenTheme.colors.subTitle,
      )
      Spacer(Modifier.height(24.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        color = RavenTheme.colors.input,
        border = BorderStroke(1.dp, RavenTheme.colors.borderStrong),
      ) {
        BasicTextField(
          value = text,
          onValueChange = { text = it },
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          textStyle = TextStyle(
            fontSize = 15.sp,
            letterSpacing = (-0.075).sp,
            color = RavenTheme.colors.subTitle,
          ),
          decorationBox = { innerTextField ->
            if (text.isEmpty()) {
              Text(
                text = "Write a note...",
                fontSize = 15.sp,
                letterSpacing = (-0.075).sp,
                color = RavenTheme.colors.caption,
              )
            }
            innerTextField()
          },
        )
      }
      Spacer(Modifier.weight(1f))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable {
            onSave(state.bookmarkId, text)
            onDismiss()
          },
        shape = RoundedCornerShape(12.dp),
        color = RavenTheme.colors.primary,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Save Note",
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
