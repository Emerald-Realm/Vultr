package voice.features.settings

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.GridMode
import voice.core.data.ThemeMode
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.AnalyticsConsentStore
import voice.core.data.store.AutoRewindAmountStore
import voice.core.data.store.DarkThemeStore
import voice.core.data.store.DeveloperMenuUnlockedStore
import voice.core.data.store.GridModeStore
import voice.core.data.store.SeekTimeStore
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.featureflag.FeatureFlag
import voice.core.featureflag.FolderPickerInSettingsFeatureFlagQualifier
import voice.core.ui.GridCount
import voice.navigation.Destination
import voice.navigation.Navigator
import java.time.LocalTime

@Inject
class SettingsViewModel(
  @DarkThemeStore
  private val themeModeStore: DataStore<ThemeMode>,
  @AutoRewindAmountStore
  private val autoRewindAmountStore: DataStore<Int>,
  @SeekTimeStore
  private val seekTimeStore: DataStore<Int>,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val gridCount: GridCount,
  @FolderPickerInSettingsFeatureFlagQualifier
  private val folderPickerInSettingsFeatureFlag: FeatureFlag<Boolean>,
  @DeveloperMenuUnlockedStore
  private val developerMenuUnlockedStore: DataStore<Boolean>,
  dispatcherProvider: DispatcherProvider,
) : SettingsListener {

  private val mainScope = MainScope(dispatcherProvider)
  private val _viewEffects = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
  internal val viewEffects: SharedFlow<SettingsViewEffect> = _viewEffects.asSharedFlow()
  private val dialog = mutableStateOf<SettingsViewState.Dialog?>(null)
  private var appVersionTapCount = 0

  @Composable
  fun viewState(): SettingsViewState {
    val themeMode by remember { themeModeStore.data }.collectAsState(initial = ThemeMode.FollowSystem)
    val autoRewindAmount by remember { autoRewindAmountStore.data }.collectAsState(initial = 0)
    val seekTime by remember { seekTimeStore.data }.collectAsState(initial = 0)
    val gridMode by remember { gridModeStore.data }.collectAsState(initial = GridMode.GRID)
    val autoSleepTimer by remember { sleepTimerPreferenceStore.data }.collectAsState(
      initial = SleepTimerPreference.Default,
    )
    val analyticsEnabled by remember { analyticsConsentStore.data }.collectAsState(initial = false)
    val showFolderPickerEntry = remember {
      folderPickerInSettingsFeatureFlag.get()
    }
    val showDeveloperMenu by remember { developerMenuUnlockedStore.data }.collectAsState(initial = false)
    return SettingsViewState(
      themeMode = themeMode,
      seekTimeInSeconds = seekTime,
      autoRewindInSeconds = autoRewindAmount,
      dialog = dialog.value,
      appVersion = appInfoProvider.versionName,
      useGrid = when (gridMode) {
        GridMode.LIST -> false
        GridMode.GRID -> true
        GridMode.FOLLOW_DEVICE -> gridCount.useGridAsDefault()
      },
      gridMode = gridMode,
      autoSleepTimer = SettingsViewState.AutoSleepTimerViewState(
        enabled = autoSleepTimer.autoSleepTimerEnabled,
        startTime = autoSleepTimer.autoSleepStartTime,
        endTime = autoSleepTimer.autoSleepEndTime,
      ),
      analyticsEnabled = analyticsEnabled,
      showAnalyticSetting = appInfoProvider.analyticsIncluded,
      showFolderPickerEntry = showFolderPickerEntry,
      showDeveloperMenu = showDeveloperMenu,
    )
  }

  override fun close() {
    navigator.goBack()
  }

  override fun onThemeRowClick() {
    dialog.value = SettingsViewState.Dialog.Theme
  }

  override fun setThemeMode(themeMode: ThemeMode) {
    mainScope.launch {
      themeModeStore.updateData { themeMode }
    }
    dialog.value = null
  }

  override fun openWebsite() {
    navigator.goTo(Destination.Website(SITE_BASE))
  }

  override fun openTermsOfService() {
    navigator.goTo(Destination.Website("$SITE_BASE/terms"))
  }

  override fun openPrivacyPolicy() {
    navigator.goTo(Destination.Website("$SITE_BASE/privacy-policy"))
  }

  override fun openOpenSourceLicenses() {
    navigator.goTo(Destination.Website("$SITE_BASE/licenses"))
  }

  override fun toggleGrid() {
    mainScope.launch {
      gridModeStore.updateData { currentMode ->
        when (currentMode) {
          GridMode.LIST -> GridMode.GRID
          GridMode.GRID -> GridMode.LIST
          GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
            GridMode.LIST
          } else {
            GridMode.GRID
          }
        }
      }
    }
  }

  override fun onLayoutRowClick() {
    dialog.value = SettingsViewState.Dialog.Layout
  }

  override fun setGridMode(gridMode: GridMode) {
    mainScope.launch {
      gridModeStore.updateData { gridMode }
    }
    dialog.value = null
  }

  override fun seekAmountChanged(seconds: Int) {
    mainScope.launch {
      seekTimeStore.updateData { seconds }
    }
  }

  override fun onSeekAmountRowClick() {
    dialog.value = SettingsViewState.Dialog.SeekTime
  }

  override fun autoRewindAmountChang(seconds: Int) {
    mainScope.launch {
      autoRewindAmountStore.updateData { seconds }
    }
  }

  override fun onAutoRewindRowClick() {
    dialog.value = SettingsViewState.Dialog.AutoRewindAmount
  }

  override fun dismissDialog() {
    dialog.value = null
  }

  override fun getSupport() {
    navigator.goTo(Destination.Website("https://github.com/Emerald-Realm/Vultr/discussions"))
  }

  override fun suggestIdea() {
    navigator.goTo(Destination.Website("https://github.com/Emerald-Realm/Vultr/discussions"))
  }

  override fun openBugReport() {
    val url = "https://github.com/Emerald-Realm/Vultr/issues/new".toUri()
      .buildUpon()
      .appendQueryParameter("version", appInfoProvider.versionName)
      .appendQueryParameter("androidversion", Build.VERSION.SDK_INT.toString())
      .appendQueryParameter("device", Build.MODEL)
      .toString()
    navigator.goTo(Destination.Website(url))
  }

  override fun openTranslations() {
    dismissDialog()
    navigator.goTo(Destination.Website("https://github.com/Emerald-Realm/Vultr"))
  }

  override fun openFaq() {
    navigator.goTo(Destination.Website("$SITE_BASE/faq"))
  }

  override fun openFolderPicker() {
    navigator.goTo(Destination.FolderPicker)
  }

  override fun setAutoSleepTimer(checked: Boolean) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepTimerEnabled = checked)
      }
    }
  }

  override fun setAutoSleepTimerStart(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepStartTime = time)
      }
    }
  }

  override fun setAutoSleepTimerEnd(time: LocalTime) {
    mainScope.launch {
      sleepTimerPreferenceStore.updateData { currentPrefs ->
        currentPrefs.copy(autoSleepEndTime = time)
      }
    }
  }

  override fun toggleAnalytics() {
    mainScope.launch {
      analyticsConsentStore.updateData { !it }
    }
  }

  override fun onAppVersionClick() {
    mainScope.launch {
      if (developerMenuUnlockedStore.data.first()) {
        return@launch
      }
      if (++appVersionTapCount >= 13) {
        developerMenuUnlockedStore.updateData { true }
        _viewEffects.emit(SettingsViewEffect.DeveloperMenuUnlocked)
      }
    }
  }

  override fun openDeveloperMenu() {
    navigator.goTo(Destination.DeveloperSettings)
  }
}

// GitHub Pages site served from the /docs folder of the public repo.
// Enable Pages: repo Settings → Pages → Source: main / "/docs".
private const val SITE_BASE = "https://emerald-realm.github.io/Vultr"
