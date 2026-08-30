package voice.features.settings.views
import voice.core.ui.RavenTheme

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import coil.compose.AsyncImage
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
import voice.features.settings.profile.ProfileGraph
import voice.features.settings.profile.ProfileViewModel
import voice.features.settings.profile.ProfileViewState
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
      ProfileViewState(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  profileViewState: ProfileViewState,
  profileViewModel: ProfileViewModel? = null,
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
        .fillMaxWidth()
        .padding(contentPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp),
    ) {
      if (profileViewModel != null) {
        SettingsProfileHeader(profileViewState, profileViewModel)
      }

      // Appearance
      SettingsSectionHeader(stringResource(StringsR.string.settings_section_appearance))
      SettingsSectionContent {
        ThemeRow(viewState.themeMode, listener::onThemeRowClick)
        SettingsRow(
          label = "Layout",
          value = if (viewState.useGrid) "Grid" else "List",
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
        SettingsRow(
          label = stringResource(StringsR.string.settings_terms_of_service),
          trailing = SettingsRowTrailing.ExternalLink,
          onClick = { listener.openTermsOfService() },
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_privacy_policy),
          trailing = SettingsRowTrailing.ExternalLink,
          onClick = { listener.openPrivacyPolicy() },
        )
        SettingsRow(
          label = stringResource(StringsR.string.settings_open_source_licenses),
          trailing = SettingsRowTrailing.ExternalLink,
          onClick = { listener.openOpenSourceLicenses() },
        )
        AppVersion(
          appVersion = viewState.appVersion,
          onClick = listener::onAppVersionClick,
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
private fun SettingsProfileHeader(viewState: ProfileViewState, viewModel: ProfileViewModel) {
  val context = LocalContext.current
  val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } catch (_: SecurityException) {
        // Some document providers do not offer persistable permissions.
      }
      viewModel.updateImage(uri.toString())
    }
  }
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(RavenTheme.colors.bgStyle)
          .clickable(onClickLabel = "Change profile picture") {
            imagePicker.launch(arrayOf("image/*"))
          },
        contentAlignment = Alignment.Center,
      ) {
        if (viewState.imageUri == null) {
          Icon(
            painterResource(UiR.drawable.ic_mage_user_circle),
            contentDescription = "Profile picture",
            modifier = Modifier.size(60.dp),
            tint = RavenTheme.colors.support,
          )
        } else {
          AsyncImage(
            model = viewState.imageUri,
            contentDescription = "Profile picture",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
          )
        }
      }
      Icon(
        painterResource(UiR.drawable.ic_mage_camera),
        contentDescription = "Change profile picture",
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size(20.dp)
          .clickable { imagePicker.launch(arrayOf("image/*")) },
        tint = RavenTheme.colors.primary,
      )
    }
    Row(
      modifier = Modifier.padding(top = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painterResource(UiR.drawable.ic_mage_star_square_fill),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = Color.Unspecified,
      )
      Spacer(Modifier.width(5.dp))
      Text("Lvl ${viewState.level} Reader", fontSize = 13.sp, color = RavenTheme.colors.caption)
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Exp", fontSize = 12.sp, color = RavenTheme.colors.caption)
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(RavenTheme.colors.bgTertiary),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(viewState.booksIntoLevel / 5f)
            .height(6.dp)
            .background(RavenTheme.colors.primary),
        )
      }
      Text("${viewState.booksIntoLevel}/5 Books", fontSize = 12.sp, color = RavenTheme.colors.caption)
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      SettingsStatTile(
        modifier = Modifier.weight(1f),
        icon = UiR.drawable.ic_mage_book_text_fill,
        label = "Books Read",
        value = viewState.completedBooks.toString(),
        tint = RavenTheme.colors.successBase,
      )
      SettingsStatTile(
        modifier = Modifier.weight(1f),
        icon = UiR.drawable.ic_mage_clock_fill,
        label = "Hours Listened",
        value = viewState.listenedHours.toString(),
        tint = Color(0xFFEE46BC),
      )
    }
  }
}

@Composable
private fun SettingsStatTile(
  modifier: Modifier,
  icon: Int,
  label: String,
  value: String,
  tint: Color,
) {
  Column(
    modifier = modifier
      .height(88.dp)
      .background(RavenTheme.colors.bgSecondary, RoundedCornerShape(8.dp))
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Unspecified)
      Spacer(Modifier.width(8.dp))
      Text(label, fontSize = 12.sp, color = RavenTheme.colors.caption)
    }
    Text(value, fontSize = 30.sp, color = RavenTheme.colors.title)
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
  val profileViewModel = retain<ProfileViewModel> { rootGraphAs<ProfileGraph>().profileViewModel }
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
  Settings(viewState, viewModel, profileViewModel.viewState(), profileViewModel, snackbarHostState)
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
