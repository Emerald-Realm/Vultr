package voice.features.settings.views
import voice.core.ui.RavenTheme

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.data.GridMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LayoutPickerDialog(
  selected: GridMode,
  onSelect: (GridMode) -> Unit,
  onDismiss: () -> Unit,
) {
  var currentSelection by remember { mutableStateOf(selected) }
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
        text = "Layout",
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.09).sp,
        textAlign = TextAlign.Center,
        color = RavenTheme.colors.title,
      )
      Spacer(Modifier.height(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        LayoutOptionCard(
          modifier = Modifier.weight(1f),
          label = "Row",
          selected = currentSelection == GridMode.LIST,
          onClick = { currentSelection = GridMode.LIST },
        ) { RowLayoutIcon(it) }
        LayoutOptionCard(
          modifier = Modifier.weight(1f),
          label = "2×2 Grid",
          selected = currentSelection == GridMode.GRID,
          onClick = { currentSelection = GridMode.GRID },
        ) { GridLayoutIcon(it, 2) }
        LayoutOptionCard(
          modifier = Modifier.weight(1f),
          label = "3×3 Grid",
          selected = currentSelection == GridMode.FOLLOW_DEVICE,
          onClick = { currentSelection = GridMode.FOLLOW_DEVICE },
        ) { GridLayoutIcon(it, 3) }
      }
      Spacer(Modifier.height(24.dp))
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clickable {
            onSelect(currentSelection)
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

@Composable
private fun LayoutOptionCard(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: @Composable (Color) -> Unit,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .clickable(onClick = onClick),
      shape = RoundedCornerShape(8.dp),
      border = BorderStroke(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.borderStrong,
      ),
      color = if (selected) RavenTheme.colors.primaryFaint else RavenTheme.colors.bgModal,
    ) {
      Box(contentAlignment = Alignment.Center) {
        icon(if (selected) RavenTheme.colors.primary else RavenTheme.colors.subTitle)
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = (-0.06).sp,
      textAlign = TextAlign.Center,
      color = if (selected) RavenTheme.colors.primary else RavenTheme.colors.subTitle,
    )
  }
}

@Composable
private fun RowLayoutIcon(color: Color) {
  Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
    repeat(4) {
      Surface(
        modifier = Modifier
          .size(width = 44.dp, height = 7.dp),
        shape = RoundedCornerShape(3.dp),
        color = color,
      ) {}
    }
  }
}

@Composable
private fun GridLayoutIcon(color: Color, count: Int) {
  val size = if (count == 2) 20.dp else 14.dp
  val gap = if (count == 2) 4.dp else 3.dp
  Column(verticalArrangement = Arrangement.spacedBy(gap)) {
    repeat(count) {
      Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        repeat(count) {
          Surface(
            modifier = Modifier.size(size),
            shape = RoundedCornerShape(2.dp),
            color = color,
          ) {}
        }
      }
    }
  }
}
