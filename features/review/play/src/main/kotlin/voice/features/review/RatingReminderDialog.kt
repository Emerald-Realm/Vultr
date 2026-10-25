package voice.features.review

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import voice.core.strings.R as StringsR

@Composable
internal fun RatingReminderDialog(
  onRate: () -> Unit,
  onNotNow: () -> Unit,
  onAlreadyRated: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(StringsR.string.rating_reminder_title)) },
    text = { Text(stringResource(StringsR.string.rating_reminder_content)) },
    confirmButton = {
      TextButton(onClick = onRate) {
        Text(stringResource(StringsR.string.rating_reminder_rate))
      }
    },
    dismissButton = {
      TextButton(onClick = onNotNow) {
        Text(stringResource(StringsR.string.rating_reminder_not_now))
      }
      TextButton(onClick = onAlreadyRated) {
        Text(stringResource(StringsR.string.rating_reminder_already_rated))
      }
    },
  )
}

@Composable
@Preview
private fun RatingReminderDialogPreview() {
  RatingReminderDialog(
    onRate = {},
    onNotNow = {},
    onAlreadyRated = {},
    onDismiss = {},
  )
}
