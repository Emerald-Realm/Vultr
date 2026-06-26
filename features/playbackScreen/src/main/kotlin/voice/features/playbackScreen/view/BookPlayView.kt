package voice.features.playbackScreen.view

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import voice.core.ui.VoiceTheme
import voice.features.playbackScreen.BookPlayViewState
import java.text.DecimalFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Shows up to two decimals, trimming trailing zeros: 1 → "1×", 1.5 → "1.5×", 1.15 → "1.15×".
private val speedFormat = DecimalFormat("0.##")

private fun formatSpeed(speed: Float): String {
  return speedFormat.format(speed) + "×"
}

@Composable
internal fun BookPlayView(
  viewState: BookPlayViewState,
  useLandscapeLayout: Boolean,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSeek: (Duration) -> Unit,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onAddBookmarkClick: () -> Unit,
  onHistoryClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onCloseClick: () -> Unit,
  onCurrentChapterClick: () -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  Scaffold(
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      BookPlayAppBar(
        viewState = viewState,
        onSleepTimerClick = onSleepTimerClick,
        onBookmarkClick = onBookmarkClick,
        onBookmarkLongClick = onBookmarkLongClick,
        onAddBookmarkClick = onAddBookmarkClick,
        onSpeedChangeClick = onSpeedChangeClick,
        onSkipSilenceClick = onSkipSilenceClick,
        onVolumeBoostClick = onVolumeBoostClick,
        onCloseClick = onCloseClick,
        useLandscapeLayout = useLandscapeLayout,
      )
    },
    bottomBar = {
      if (!useLandscapeLayout) {
        PlayerActionsBar(
          speedText = formatSpeed(viewState.playbackSpeed),
          speedActive = viewState.playbackSpeed != 1.0f,
          sleepTimerState = viewState.sleepTimerState,
          onSpeedClick = onSpeedChangeClick,
          onBookmarksClick = onBookmarkClick,
          onHistoryClick = onHistoryClick,
          onSleepClick = onSleepTimerClick,
        )
      }
    },
    content = {
      BookPlayContent(
        contentPadding = it,
        viewState = viewState,
        onPlayClick = onPlayClick,
        onRewindClick = onRewindClick,
        onFastForwardClick = onFastForwardClick,
        onSeek = onSeek,
        onSkipToNext = onSkipToNext,
        onSkipToPrevious = onSkipToPrevious,
        onCurrentChapterClick = onCurrentChapterClick,
        useLandscapeLayout = useLandscapeLayout,
      )
    },
  )
}

@Composable
@Preview
private fun BookPlayPreview(
  @PreviewParameter(BookPlayViewStatePreviewProvider::class)
  viewState: BookPlayViewState,
) {
  VoiceTheme {
    BookPlayView(
      viewState = viewState,
      onPlayClick = {},
      onRewindClick = {},
      onFastForwardClick = {},
      onSeek = {},
      onSleepTimerClick = {},
      onBookmarkClick = {},
      onBookmarkLongClick = {},
      onAddBookmarkClick = {},
      onHistoryClick = {},
      onSpeedChangeClick = {},
      onSkipSilenceClick = {},
      onVolumeBoostClick = {},
      onSkipToNext = {},
      onSkipToPrevious = {},
      onCloseClick = {},
      onCurrentChapterClick = {},
      useLandscapeLayout = false,
    )
  }
}

private class BookPlayViewStatePreviewProvider : PreviewParameterProvider<BookPlayViewState> {
  override val values = sequence {
    val initial = BookPlayViewState(
      chapterName = "My Chapter",
      showPreviousNextButtons = false,
      cover = null,
      duration = 10.minutes,
      playedTime = 3.minutes,
      playing = true,
      skipSilence = true,
      sleepTimerState = BookPlayViewState.SleepTimerViewState.Disabled,
      title = "Das Ende der Welt",
      author = "Max Mustermann",
      playbackSpeed = 1.2f,
    )
    yield(initial)
    yield(
      initial.copy(
        showPreviousNextButtons = !initial.showPreviousNextButtons,
        playing = !initial.playing,
        skipSilence = !initial.skipSilence,
      ),
    )
    yield(initial.copy(chapterName = null))
  }
}
