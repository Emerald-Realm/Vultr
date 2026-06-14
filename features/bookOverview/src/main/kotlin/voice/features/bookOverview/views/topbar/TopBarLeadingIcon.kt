package voice.features.bookOverview.views.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.R as UiR

@Composable
internal fun ColumnScope.TopBarLeadingIcon(
  searchActive: Boolean,
  onActiveChange: (Boolean) -> Unit,
) {
  AnimatedVisibility(
    visible = searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    IconButton(onClick = { onActiveChange(false) }) {
      Icon(
        painter = painterResource(UiR.drawable.ic_mage_arrow_left),
        contentDescription = stringResource(id = R.string.close),
      )
    }
  }
  AnimatedVisibility(
    visible = !searchActive,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
      Icon(
        painter = painterResource(UiR.drawable.ic_mage_search),
        contentDescription = stringResource(id = R.string.search_hint),
      )
    }
  }
}
