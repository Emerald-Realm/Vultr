package voice.features.folderPicker.addcontent

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
internal fun SelectFolderButton(
  @DrawableRes icon: Int,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  OutlinedButton(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(999.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      painter = painterResource(id = icon),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
    Text(text = text, style = MaterialTheme.typography.bodyMedium)
  }
}
