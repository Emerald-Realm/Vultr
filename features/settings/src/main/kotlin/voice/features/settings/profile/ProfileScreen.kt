package voice.features.settings.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import coil.compose.AsyncImage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.RavenTheme
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.ui.R as UiR

@Composable
private fun ProfileScreen(viewState: ProfileViewState, viewModel: ProfileViewModel) {
  val context = LocalContext.current
  var editingName by remember { mutableStateOf(false) }
  val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } catch (_: SecurityException) {
        // Some document providers grant access without offering persistable permissions.
      }
      viewModel.updateImage(uri.toString())
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Profile", style = MaterialTheme.typography.headlineSmall) },
        navigationIcon = {
          IconButton(onClick = viewModel::close) {
            Icon(painterResource(UiR.drawable.ic_mage_arrow_left), contentDescription = "Back")
          }
        },
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(24.dp))
      Box(
        modifier = Modifier
          .size(116.dp)
          .background(RavenTheme.colors.bgStyle)
          .border(1.dp, RavenTheme.colors.borderStrong, CircleShape)
          .clickable(onClickLabel = "Change profile picture") { imagePicker.launch(arrayOf("image/*")) },
        contentAlignment = Alignment.Center,
      ) {
        if (viewState.imageUri == null) {
          Icon(
            painterResource(UiR.drawable.ic_mage_user),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = RavenTheme.colors.support,
          )
        } else {
          AsyncImage(
            model = viewState.imageUri,
            contentDescription = "Profile picture",
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop,
          )
        }
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(32.dp)
            .background(RavenTheme.colors.primary, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            painterResource(UiR.drawable.ic_mage_camera),
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = RavenTheme.colors.white,
          )
        }
      }

      Text(
        text = viewState.name,
        modifier = Modifier
          .padding(top = 18.dp)
          .clickable(onClickLabel = "Edit name") { editingName = true }
          .padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painterResource(UiR.drawable.ic_mage_star_circle),
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = RavenTheme.colors.warningBase,
        )
        Spacer(Modifier.width(6.dp))
        Text("Level ${viewState.level} Reader", color = RavenTheme.colors.caption)
      }

      Spacer(Modifier.height(34.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatPanel(
          modifier = Modifier.weight(1f),
          icon = UiR.drawable.ic_mage_book,
          label = "Books completed",
          value = viewState.completedBooks.toString(),
        )
        StatPanel(
          modifier = Modifier.weight(1f),
          icon = UiR.drawable.ic_mage_clock,
          label = "Hours listened",
          value = viewState.listenedHours.toString(),
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 28.dp)
          .background(RavenTheme.colors.bgSecondary, RoundedCornerShape(8.dp))
          .padding(18.dp),
      ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Level progress", fontWeight = FontWeight.Medium, color = RavenTheme.colors.title)
          Text(
            "${viewState.booksIntoLevel} / $BOOKS_PER_LEVEL books",
            color = RavenTheme.colors.support,
          )
        }
        LinearProgressIndicator(
          progress = { viewState.booksIntoLevel / BOOKS_PER_LEVEL.toFloat() },
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(8.dp)
            .clip(CircleShape),
          color = RavenTheme.colors.primary,
          trackColor = RavenTheme.colors.bgTertiary,
        )
        Text(
          text = if (viewState.booksIntoLevel == 0 && viewState.completedBooks > 0) {
            "Level ${viewState.level} reached"
          } else {
            "${BOOKS_PER_LEVEL - viewState.booksIntoLevel} more to reach level ${viewState.level + 1}"
          },
          modifier = Modifier.padding(top = 12.dp),
          style = MaterialTheme.typography.bodyMedium,
          color = RavenTheme.colors.caption,
        )
      }
    }
  }

  if (editingName) {
    EditNameDialog(
      currentName = viewState.name,
      onDismiss = { editingName = false },
      onSave = {
        viewModel.updateName(it)
        editingName = false
      },
    )
  }
}

@Composable
private fun StatPanel(modifier: Modifier, icon: Int, label: String, value: String) {
  Column(
    modifier = modifier
      .height(132.dp)
      .background(RavenTheme.colors.bgSecondary, RoundedCornerShape(8.dp))
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(painterResource(icon), contentDescription = null, tint = RavenTheme.colors.primary)
      Spacer(Modifier.width(8.dp))
      Text(label, style = MaterialTheme.typography.bodySmall, color = RavenTheme.colors.caption)
    }
    Text(value, style = MaterialTheme.typography.displaySmall, color = RavenTheme.colors.title)
  }
}

@Composable
private fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
  var name by remember(currentName) { mutableStateOf(currentName) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit name") },
    text = {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        label = { Text("Name") },
        singleLine = true,
      )
    },
    confirmButton = {
      Button(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@ContributesTo(AppScope::class)
interface ProfileGraph {
  val profileViewModel: ProfileViewModel
}

@ContributesTo(AppScope::class)
interface ProfileProvider {
  @Provides
  @IntoSet
  fun profileNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Profile> { key ->
    NavEntry(key) { Profile() }
  }
}

@Composable
fun Profile() {
  val viewModel = retain<ProfileViewModel> { rootGraphAs<ProfileGraph>().profileViewModel }
  ProfileScreen(viewModel.viewState(), viewModel)
}
