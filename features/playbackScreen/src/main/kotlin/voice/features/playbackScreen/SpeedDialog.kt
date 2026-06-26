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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs
import voice.core.ui.RavenTheme
import voice.core.ui.R as UiR

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
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = {
            viewModel.onPlaybackSpeedChanged((dialogState.speed - 0.05f).coerceIn(valueRange))
          },
        ) {
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_minus_circle),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
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
          Icon(
            painter = painterResource(UiR.drawable.ic_mage_plus_circle),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable { viewModel.dismissDialog() },
        shape = RoundedCornerShape(12.dp),
        color = RavenTheme.colors.primary,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Save Settings",
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

@Composable
private fun SpeedChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .height(45.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(percent = 50),
    color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.bgStyle,
    contentColor = if (selected) Color.White else RavenTheme.colors.subTitle,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.12).sp,
      )
    }
  }
}
