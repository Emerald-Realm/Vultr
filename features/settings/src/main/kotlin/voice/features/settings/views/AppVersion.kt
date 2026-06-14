package voice.features.settings.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
internal fun AppVersion(
  appVersion: String,
  onClick: () -> Unit,
) {
  SettingsRow(
    label = stringResource(StringsR.string.pref_app_version),
    value = appVersion,
    trailing = SettingsRowTrailing.None,
    onClick = onClick,
  )
}
