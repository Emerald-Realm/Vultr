package voice.features.settings

import voice.core.data.GridMode
import voice.core.data.ThemeMode
import java.time.LocalTime

data class SettingsViewState(
  val themeMode: ThemeMode,
  val seekTimeInSeconds: Int,
  val autoRewindInSeconds: Int,
  val appVersion: String,
  val dialog: Dialog?,
  val useGrid: Boolean,
  val gridMode: GridMode = GridMode.GRID,
  val autoSleepTimer: AutoSleepTimerViewState,
  val showAnalyticSetting: Boolean,
  val analyticsEnabled: Boolean,
  val showFolderPickerEntry: Boolean,
  val showDeveloperMenu: Boolean,
) {

  enum class Dialog {
    AutoRewindAmount,
    SeekTime,
    Theme,
    Layout,
  }

  companion object {
    fun preview(): SettingsViewState {
      return SettingsViewState(
        themeMode = ThemeMode.FollowSystem,
        seekTimeInSeconds = 42,
        autoRewindInSeconds = 12,
        dialog = null,
        appVersion = "1.2.3",
        useGrid = true,
        autoSleepTimer = AutoSleepTimerViewState.preview(),
        analyticsEnabled = false,
        showAnalyticSetting = true,
        showFolderPickerEntry = false,
        showDeveloperMenu = true,
      )
    }
  }

  data class AutoSleepTimerViewState(
    val enabled: Boolean,
    val startTime: LocalTime,
    val endTime: LocalTime,
  ) {
    companion object {
      fun preview(): AutoSleepTimerViewState {
        return AutoSleepTimerViewState(
          enabled = false,
          startTime = LocalTime.of(22, 0),
          endTime = LocalTime.of(6, 0),
        )
      }
    }
  }
}
