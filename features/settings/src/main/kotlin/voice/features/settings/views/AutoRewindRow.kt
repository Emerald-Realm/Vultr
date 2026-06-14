package voice.features.settings.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun AutoRewindRow(
  autoRewindInSeconds: Int,
  openAutoRewindDialog: () -> Unit,
) {
  SettingsRow(
    label = stringResource(StringsR.string.pref_auto_rewind_title),
    value = LocalResources.current.getQuantityString(
      StringsR.plurals.seconds,
      autoRewindInSeconds,
      autoRewindInSeconds,
    ),
    trailing = SettingsRowTrailing.Dots,
    onClick = openAutoRewindDialog,
  )
}

@Composable
internal fun AutoRewindAmountDialog(
  currentSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  TimeSettingDialog(
    title = stringResource(StringsR.string.pref_auto_rewind_title),
    currentSeconds = currentSeconds,
    minSeconds = 0,
    maxSeconds = 20,
    textPluralRes = StringsR.plurals.seconds,
    onSecondsConfirm = onSecondsConfirm,
    onDismiss = onDismiss,
  )
}
