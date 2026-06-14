package voice.features.settings.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun SeekTimeRow(
  seekTimeInSeconds: Int,
  openSeekTimeDialog: () -> Unit,
) {
  SettingsRow(
    label = stringResource(StringsR.string.pref_seek_time),
    value = LocalResources.current.getQuantityString(
      StringsR.plurals.seconds,
      seekTimeInSeconds,
      seekTimeInSeconds,
    ),
    trailing = SettingsRowTrailing.Dots,
    onClick = openSeekTimeDialog,
  )
}

@Composable
internal fun SeekAmountDialog(
  currentSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  TimeSettingDialog(
    title = stringResource(StringsR.string.pref_seek_time),
    currentSeconds = currentSeconds,
    minSeconds = 3,
    maxSeconds = 60,
    textPluralRes = StringsR.plurals.seconds,
    onSecondsConfirm = onSecondsConfirm,
    onDismiss = onDismiss,
  )
}
