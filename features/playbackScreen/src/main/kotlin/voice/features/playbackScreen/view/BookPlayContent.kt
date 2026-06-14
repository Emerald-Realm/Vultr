package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import voice.core.ui.RavenTheme
import voice.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration

@Composable
internal fun BookPlayContent(
  contentPadding: PaddingValues,
  viewState: BookPlayViewState,
  onPlayClick: () -> Unit,
  onRewindClick: () -> Unit,
  onFastForwardClick: () -> Unit,
  onSeek: (Duration) -> Unit,
  onSkipToNext: () -> Unit,
  onSkipToPrevious: () -> Unit,
  onCurrentChapterClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  if (useLandscapeLayout) {
    Row(Modifier.padding(contentPadding)) {
      CoverRow(
        cover = viewState.cover,
        onPlayClick = onPlayClick,
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F)
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
      )
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .weight(1F),
        verticalArrangement = Arrangement.Center,
      ) {
        PlayerHeader(title = viewState.title, author = viewState.author)
        Spacer(modifier = Modifier.size(16.dp))
        viewState.chapterName?.let { chapterName ->
          ChapterRow(chapterName = chapterName, onClick = onCurrentChapterClick)
        }
        Spacer(modifier = Modifier.size(20.dp))
        SliderRow(
          duration = viewState.duration,
          playedTime = viewState.playedTime,
          onSeek = onSeek,
        )
        Spacer(modifier = Modifier.size(16.dp))
        PlaybackRow(
          playing = viewState.playing,
          showPreviousNext = viewState.showPreviousNextButtons,
          onPlayClick = onPlayClick,
          onRewindClick = onRewindClick,
          onFastForwardClick = onFastForwardClick,
          onSkipToPrevious = onSkipToPrevious,
          onSkipToNext = onSkipToNext,
        )
      }
    }
  } else {
    Column(Modifier.padding(contentPadding)) {
      Spacer(modifier = Modifier.size(8.dp))
      PlayerHeader(title = viewState.title, author = viewState.author)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1F)
          .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
      ) {
        CoverRow(
          onPlayClick = onPlayClick,
          cover = viewState.cover,
          modifier = Modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(1F),
        )
      }
      viewState.chapterName?.let { chapterName ->
        ChapterRow(chapterName = chapterName, onClick = onCurrentChapterClick)
      }
      Spacer(modifier = Modifier.size(16.dp))
      SliderRow(
        duration = viewState.duration,
        playedTime = viewState.playedTime,
        onSeek = onSeek,
      )
      Spacer(modifier = Modifier.size(16.dp))
      PlaybackRow(
        playing = viewState.playing,
        showPreviousNext = viewState.showPreviousNextButtons,
        onPlayClick = onPlayClick,
        onRewindClick = onRewindClick,
        onFastForwardClick = onFastForwardClick,
        onSkipToPrevious = onSkipToPrevious,
        onSkipToNext = onSkipToNext,
      )
      Spacer(modifier = Modifier.size(24.dp))
    }
  }
}

@Composable
private fun PlayerHeader(
  title: String,
  author: String?,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = title,
      fontSize = 24.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = (-0.12).sp,
      textAlign = TextAlign.Center,
      color = RavenTheme.colors.title,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    if (!author.isNullOrBlank()) {
      Spacer(Modifier.size(4.dp))
      Text(
        text = author,
        fontSize = 13.sp,
        letterSpacing = (-0.065).sp,
        textAlign = TextAlign.Center,
        color = RavenTheme.colors.support,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
