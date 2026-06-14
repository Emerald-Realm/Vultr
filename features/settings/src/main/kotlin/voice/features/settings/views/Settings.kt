package voice.features.settings.views
import voice.core.ui.RavenTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.ui.VoiceTheme
import voice.features.settings.SettingsListener
import voice.features.settings.SettingsViewEffect
import voice.features.settings.SettingsViewModel
import voice.features.settings.SettingsViewState
import voice.features.settings.views.sleeptimer.AutoSleepTimerCard
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR
import voice.core.ui.R as UiR

@Composable
@Preview
private fun SettingsPreview() {
  VoiceTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  Scaffold(
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.12).sp,
          )
        },
        navigationIcon = {
          IconButton(onClick = { listener.close() }) {
            Icon(
              painter = painterResource(UiR.drawable.ic_mage_arrow_left),
              contentDescription = stringResource(StringsR.string.close),
              modifier = Modifier.size(24.dp),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    Column(
      modifier = Modifier
        .padding(contentPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp),
    ) {
      // Appearance
      SettingsSectionHeader(stringResource(StringsR.string.settings_section_appearance))
      SettingsSectionContent {
        ThemeRow(viewState.themeMode, listener::onThemeRowClick)
        SettingsRow(
          label = "Layout",
          value = if (viewState.useGrid) "Grid" else "Row",
          trailing = SettingsRowTrailing.Dots,
          onClick = { listener.onLayoutRowClick() },
        )
      }

      // Playback
      SettingsSectionHeader(stringResource(StringsR.string.settings_section_playback))
      SettingsSectionContent {
        SeekTimeRow(viewState.seekTimeInSeconds) { listener.onSeekAmountRowClick() }
        AutoRewindRow(viewState.autoRewindInSeconds) { listener.onAutoRewindRowClick() }
      }

      // Help
      SettingsSectionHeader(stringResource(StringsR.string.settings_section_help))
      SettingsSectionContent {
        SettingsRow(
          label = stringResource(StringsR.string.pref_report_issue),
          trailing = SettingsRowTrailing.ExternalLink,
          onClick = { listener.openBugReport() },
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_visit_website),
          trailing = SettingsRowTrailing.ExternalLink,
          onClick = { listener.openWebsite() },
        )
      }

      // About
      SettingsSectionHeader(stringResource(StringsR.string.settings_section_about))
      SettingsSectionContent {
        AppVersion(
          appVersion = viewState.appVersion,
          onClick = listener::onAppVersionClick,
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_terms_of_service),
          onClick = { listener.openTermsOfService() },
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_privacy_policy),
          onClick = { listener.openPrivacyPolicy() },
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_open_source_licenses),
          onClick = { listener.openOpenSourceLicenses() },
        )
      }

      if (viewState.showAnalyticSetting) {
        SettingsSectionContent {
          AnalyticsRow(
            analyticsEnabled = viewState.analyticsEnabled,
            toggle = listener::toggleAnalytics,
          )
        }
      }

      if (viewState.showDeveloperMenu) {
        SettingsSectionContent {
          DeveloperMenuItem(onClick = listener::openDeveloperMenu)
        }
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun SettingsSectionHeader(text: String) {
  Text(
    text = text,
    modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
    fontSize = 13.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = (-0.065).sp,
    color = RavenTheme.colors.primary,
  )
}

@Composable
private fun SettingsSectionContent(content: @Composable () -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth(),
  ) {
    content()
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  SettingsRow(
    label = stringResource(StringsR.string.settings_analytics_consent_title),
    onClick = toggle,
  )
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun settingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = retain<SettingsViewModel> { rootGraphAs<SettingsGraph>().settingsViewModel }
  val snackbarHostState = remember { SnackbarHostState() }
  val viewState = viewModel.viewState()
  val currentDeveloperMenuUnlockedMessage = rememberUpdatedState("Developer Menu unlocked")
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        SettingsViewEffect.DeveloperMenuUnlocked -> {
          snackbarHostState.showSnackbar(currentDeveloperMenuUnlockedMessage.value)
        }
      }
    }
  }
  Settings(viewState, viewModel, snackbarHostState)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SeekTime -> {
      SeekAmountDialog(
        currentSeconds = viewState.seekTimeInSeconds,
        onSecondsConfirm = listener::seekAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Theme -> {
      ThemePickerDialog(
        selected = viewState.themeMode,
        onSelect = listener::setThemeMode,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Layout -> {
      LayoutPickerDialog(
        selected = viewState.gridMode,
        onSelect = listener::setGridMode,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}
