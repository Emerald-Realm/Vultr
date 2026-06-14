package voice.features.settings.views
import voice.core.ui.RavenTheme

import androidx.annotation.PluralsRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeSettingDialog(
  title: String,
  currentSeconds: Int,
  @PluralsRes textPluralRes: Int,
  minSeconds: Int,
  maxSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  val options = generateTimeOptions(minSeconds, maxSeconds)
  var selectedValue by remember { mutableIntStateOf(currentSeconds) }
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
        color = RavenTheme.colors.title,
      )
      Spacer(Modifier.height(24.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 3,
      ) {
        options.forEach { seconds ->
          val label = LocalResources.current.getQuantityString(textPluralRes, seconds, seconds)
          TimeChip(
            modifier = Modifier.weight(1f),
            label = label,
            selected = selectedValue == seconds,
            onClick = { selectedValue = seconds },
          )
        }
      }
      Spacer(Modifier.height(24.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable {
            onSecondsConfirm(selectedValue)
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

private fun generateTimeOptions(min: Int, max: Int): List<Int> {
  val steps = mutableListOf<Int>()
  // Skip amount: 5/10/15/20/30/60. Auto rewind on resume: 0/2/5.
  val candidates = if (max <= 20) {
    listOf(0, 2, 5)
  } else {
    listOf(5, 10, 15, 20, 30, 60)
  }
  candidates.forEach { v ->
    if (v in min..max) steps.add(v)
  }
  return steps
}

@Composable
private fun TimeChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .height(37.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(percent = 50),
    color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.bgStyle,
    contentColor = if (selected) Color.White else RavenTheme.colors.title,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.075).sp,
        textAlign = TextAlign.Center,
      )
    }
  }
}
