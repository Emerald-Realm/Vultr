package voice.features.folderPicker.addcontent

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.ui.R as UiR
import voice.features.folderPicker.folderPicker.FileTypeSelection
import voice.navigation.Origin
import voice.core.strings.R as StringsR

@Composable
internal fun SelectFolder(
  onBack: () -> Unit,
  onAdd: (FileTypeSelection, Uri) -> Unit,
  origin: Origin,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      SelectFolderAppBar(onBack)
    },
    content = { contentPadding ->
      Column(
        Modifier
          .fillMaxSize()
          .padding(contentPadding)
          .padding(top = 8.dp),
      ) {
        Surface(
          modifier = Modifier
            .padding(start = 20.dp)
            .size(44.dp),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              modifier = Modifier.size(24.dp),
              painter = painterResource(id = UiR.drawable.ic_raven_logo),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        Spacer(Modifier.size(20.dp))

        Text(
          modifier = Modifier.padding(horizontal = 20.dp),
          text = stringResource(
            when (origin) {
              Origin.Default -> StringsR.string.select_folder_title_default
              Origin.Onboarding -> StringsR.string.select_folder_title_onboarding
            },
          ),
          style = MaterialTheme.typography.headlineSmall,
          fontSize = 24.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.size(8.dp))
        Text(
          modifier = Modifier.padding(horizontal = 20.dp),
          text = stringResource(StringsR.string.select_folder_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))

        SelectFolderButtonRow(onAdd)

        Spacer(Modifier.size(32.dp))
      }
    },
  )
}

@Composable
@Preview
private fun SelectFolderPreview() {
  SelectFolder(
    onBack = {},
    onAdd = { _, _ -> },
    origin = Origin.Default,
  )
}
