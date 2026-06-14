package voice.features.playbackScreen.history
import voice.core.ui.RavenTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.material3.Icon
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import voice.core.data.ListeningHistoryAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@ContributesTo(AppScope::class)
interface HistoryGraph {
  val historyViewModelFactory: HistoryViewModel.Factory
}

@Composable
fun HistorySheetContent(
  viewState: HistoryViewState,
  onDelete: (voice.core.data.ListeningSession.Id) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
  ) {
    Text(
      text = stringResource(StringsR.string.history_title),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      fontSize = 18.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = (-0.09).sp,
      textAlign = TextAlign.Center,
      color = RavenTheme.colors.title,
    )
    if (viewState.days.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(StringsR.string.history_empty),
          fontSize = 14.sp,
          color = RavenTheme.colors.caption,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        viewState.days.forEach { day ->
          item(key = "header-${day.date}") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = dayLabel(day.date),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.06).sp,
                color = RavenTheme.colors.title,
              )
              Text(
                text = day.summary,
                fontSize = 12.sp,
                letterSpacing = (-0.06).sp,
                color = RavenTheme.colors.caption,
              )
            }
          }
          items(day.entries, key = { it.id.value }) { entry ->
            HistoryRow(entry = entry, onDelete = { onDelete(entry.id) })
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryRow(
  entry: HistoryEntryViewState,
  onDelete: () -> Unit,
) {
  var showPopup by remember { mutableStateOf(false) }
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(6.dp),
    color = Color(0xFFF9FAFB),
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = buildString {
            if (entry.chapterName != null) {
              append(entry.chapterName)
              append(" - ")
            }
            append(entry.positionText)
          },
          fontSize = 12.sp,
          letterSpacing = (-0.06).sp,
          color = RavenTheme.colors.subTitle,
        )
        Text(
          text = "${actionLabel(entry.action)} · ${entry.timeText}",
          fontSize = 12.sp,
          letterSpacing = (-0.06).sp,
          color = RavenTheme.colors.caption,
        )
      }
      Box {
        Icon(
          painter = painterResource(UiR.drawable.ic_mage_dots),
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .clickable { showPopup = true },
          tint = RavenTheme.colors.icon,
        )
        if (showPopup) {
          Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(0, 80),
            onDismissRequest = { showPopup = false },
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color.White,
              shadowElevation = 4.dp,
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F5F6)),
            ) {
              Text(
                text = stringResource(StringsR.string.delete),
                modifier = Modifier
                  .width(200.dp)
                  .clickable {
                    showPopup = false
                    onDelete()
                  }
                  .padding(10.dp),
                fontSize = 14.sp,
                letterSpacing = (-0.07).sp,
                color = RavenTheme.colors.subTitle,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun actionLabel(action: ListeningHistoryAction?): String {
  val resId = when (action) {
    ListeningHistoryAction.Played -> StringsR.string.history_action_played
    ListeningHistoryAction.Paused -> StringsR.string.history_action_paused
    ListeningHistoryAction.Jumped -> StringsR.string.history_action_jumped
    ListeningHistoryAction.SkippedToChapter -> StringsR.string.history_action_skipped_to_chapter
    ListeningHistoryAction.NewChapter -> StringsR.string.history_action_new_chapter
    ListeningHistoryAction.SleepTimer -> StringsR.string.history_action_sleep_timer
    null -> return ""
  }
  return stringResource(resId)
}

@Composable
private fun dayLabel(date: LocalDate): String {
  return when (date) {
    LocalDate.now() -> stringResource(StringsR.string.bookmark_today)
    LocalDate.now().minusDays(1) -> stringResource(StringsR.string.bookmark_yesterday)
    else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
  }
}
