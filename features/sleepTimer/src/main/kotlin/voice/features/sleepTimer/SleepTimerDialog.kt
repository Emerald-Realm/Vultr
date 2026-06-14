package voice.features.sleepTimer
import voice.core.ui.RavenTheme

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.strings.R as StringsR

private val sleepTimeOptions = listOf(5, 10, 15, 20, 25, 30, 45, 60)
private const val END_OF_CHAPTER = -1

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SleepTimerDialog(
  viewState: SleepTimerViewState,
  onDismiss: () -> Unit,
  onIncrementSleepTime: () -> Unit,
  onDecrementSleepTime: () -> Unit,
  onAcceptSleepTime: (Int) -> Unit,
  onAcceptSleepAtEndOfChapter: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTime by remember { mutableIntStateOf(viewState.customSleepTime) }
  ModalBottomSheet(
    modifier = modifier,
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
        text = stringResource(id = StringsR.string.sleep_timer_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(24.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 3,
      ) {
        sleepTimeOptions.forEach { time ->
          SleepChip(
            modifier = Modifier.weight(1f),
            label = "$time Min",
            selected = selectedTime == time,
            onClick = { selectedTime = time },
          )
        }
        SleepChip(
          modifier = Modifier.weight(1f),
          label = stringResource(id = StringsR.string.end_of_chapter),
          selected = selectedTime == END_OF_CHAPTER,
          onClick = { selectedTime = END_OF_CHAPTER },
        )
      }
      Spacer(Modifier.height(24.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable {
            if (selectedTime == END_OF_CHAPTER) {
              onAcceptSleepAtEndOfChapter()
            } else {
              onAcceptSleepTime(selectedTime)
            }
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

@Composable
private fun SleepChip(
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
