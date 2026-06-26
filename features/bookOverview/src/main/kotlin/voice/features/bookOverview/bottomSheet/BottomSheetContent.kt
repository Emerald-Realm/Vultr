package voice.features.bookOverview.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import voice.core.ui.RavenTheme

@Composable
internal fun BottomSheetContent(
  state: EditBookBottomSheetState,
  onItemClick: (BottomSheetItem) -> Unit,
) {
  Column {
    state.items.forEach { item ->
      val tint = if (item.destructive) RavenTheme.colors.errorBase else RavenTheme.colors.title
      ListItem(
        colors = ListItemDefaults.colors(containerColor = RavenTheme.colors.bgModal),
        modifier = Modifier.clickable {
          onItemClick(item)
        },
        headlineContent = {
          Text(text = item.title, color = tint)
        },
        leadingContent = {
          Icon(
            painter = painterResource(item.iconRes),
            contentDescription = item.title,
            tint = tint,
          )
        },
      )
    }
    Spacer(modifier = Modifier.size(24.dp))
  }
}
