package voice.core.ui

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import voice.core.data.ThemeMode
import voice.core.data.store.DarkThemeStore

@ContributesTo(AppScope::class)
interface SharedGraph {

  @get:DarkThemeStore
  val themeModeStore: DataStore<ThemeMode>
}
