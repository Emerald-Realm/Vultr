package voice.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import voice.core.common.rootGraphAs
import voice.core.data.ThemeMode

@Composable
fun isDarkTheme(): Boolean {
  val themeModeFlow = remember {
    rootGraphAs<SharedGraph>().themeModeStore.data
  }
  val themeMode = themeModeFlow.collectAsState(
    initial = ThemeMode.FollowSystem,
    context = Dispatchers.Unconfined,
  ).value
  return when (themeMode) {
    ThemeMode.FollowSystem -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
  }
}
