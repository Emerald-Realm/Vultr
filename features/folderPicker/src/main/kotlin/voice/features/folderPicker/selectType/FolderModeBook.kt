package voice.features.folderPicker.selectType

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
internal fun FolderModeBook(
  book: SelectFolderTypeViewState.Book,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Icon(
        modifier = Modifier.padding(bottom = 8.dp),
        painter = painterResource(id = UiR.drawable.ic_mage_sound_waves),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
      Text(
        text = book.name,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        modifier = Modifier.padding(top = 2.dp),
        text = pluralStringResource(
          id = StringsR.plurals.folder_type_file_count,
          count = book.fileCount,
          book.fileCount,
        ),
        style = MaterialTheme.typography.bodySmall,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
