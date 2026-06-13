package voice.features.playbackScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import kotlin.math.abs

private val speedPresets = listOf(
  "0.75x" to 0.75f,
  "1.0x" to 1.0f,
  "1.1x" to 1.1f,
  "1.2x" to 1.2f,
  "1.5x" to 1.5f,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpeedDialog(
  dialogState: BookPlayDialogViewState.SpeedDialog,
  viewModel: BookPlayViewModel,
) {
  val format = remember { DecimalFormat("0.##") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val valueRange = 0.5f..dialogState.maxSpeed
  ModalBottomSheet(
    onDismissRequest = { viewModel.dismissDialog() },
    sheetState = sheetState,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .navigationBarsPadding()
        .padding(bottom = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = "Reading Speed: ${format.format(dialogState.speed)}x",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(20.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = {
            viewModel.onPlaybackSpeedChanged((dialogState.speed - 0.05f).coerceIn(valueRange))
          },
        ) {
          Icon(Icons.Filled.Remove, contentDescription = null)
        }
        Slider(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp),
          value = dialogState.speed.coerceIn(valueRange),
          valueRange = valueRange,
          onValueChange = { viewModel.onPlaybackSpeedChanged(it) },
        )
        IconButton(
          onClick = {
            viewModel.onPlaybackSpeedChanged((dialogState.speed + 0.05f).coerceIn(valueRange))
          },
        ) {
          Icon(Icons.Filled.Add, contentDescription = null)
        }
      }
      Spacer(Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        speedPresets.forEach { (label, value) ->
          SpeedChip(
            modifier = Modifier.weight(1f),
            label = label,
            selected = abs(dialogState.speed - value) < 0.001f,
            onClick = { viewModel.onPlaybackSpeedChanged(value) },
          )
        }
      }
      Spacer(Modifier.height(24.dp))
      Button(
        onClick = { viewModel.dismissDialog() },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
      ) {
        Text("Save Settings")
      }
    }
  }
}

@Composable
private fun SpeedChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(percent = 50),
    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
  ) {
    Text(
      text = label,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}
