package voice.features.folderPicker.addcontent

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.ui.R as UiR
import voice.core.ui.RavenTheme
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
          color = RavenTheme.colors.primaryFaint,
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

        Spacer(Modifier.size(36.dp))

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

        Spacer(Modifier.size(10.dp))

        InstructionRow(
          icon = UiR.drawable.ic_mage_check_circle,
          iconTint = Color(0xFF26BD01),
          text = stringResource(StringsR.string.select_folder_supported_files),
        )
        Spacer(Modifier.size(10.dp))
        InstructionRow(
          icon = UiR.drawable.ic_mage_exclamation_circle,
          iconTint = Color(0xFFFF9811),
          text = stringResource(StringsR.string.select_folder_pdf_warning),
        )

        Spacer(Modifier.weight(1f))

        SelectFolderButtonRow(onAdd)

        Spacer(Modifier.weight(1f))
      }
    },
  )
}

@Composable
private fun InstructionRow(
  icon: Int,
  iconTint: Color,
  text: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      modifier = Modifier.size(18.dp),
      painter = painterResource(icon),
      contentDescription = null,
      tint = iconTint,
    )
    Spacer(Modifier.size(6.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      color = Color(0xFF627193),
    )
  }
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
