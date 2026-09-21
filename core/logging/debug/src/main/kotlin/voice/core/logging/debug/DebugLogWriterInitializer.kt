package voice.core.logging.debug

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import voice.core.initializer.AppInitializer
import voice.core.logging.api.Logger

@ContributesIntoSet(AppScope::class)
class DebugLogWriterInitializer : AppInitializer {

  override fun onAppStart() {
    Logger.install(DebugLogWriter())
  }
}
