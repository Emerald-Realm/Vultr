package voice.features.playbackScreen.view
import voice.core.ui.RavenTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
internal fun AddBookmarkDialog(
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
) {
  var text by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = RavenTheme.colors.bgModal,
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = "Add Bookmark",
          modifier = Modifier.fillMaxWidth(),
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = (-0.09).sp,
          textAlign = TextAlign.Center,
          color = RavenTheme.colors.title,
        )
        Spacer(Modifier.height(16.dp))
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          shape = RoundedCornerShape(12.dp),
          color = RavenTheme.colors.input,
          border = BorderStroke(1.dp, RavenTheme.colors.borderStrong),
        ) {
          BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
              .focusRequester(focusRequester),
            textStyle = TextStyle(
              fontSize = 15.sp,
              letterSpacing = (-0.075).sp,
              color = RavenTheme.colors.subTitle,
            ),
            decorationBox = { innerTextField ->
              if (text.isEmpty()) {
                Text(
                  text = "Write a note...",
                  fontSize = 15.sp,
                  letterSpacing = (-0.075).sp,
                  color = RavenTheme.colors.caption,
                )
              }
              innerTextField()
            },
          )
        }
        Spacer(Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Surface(
            modifier = Modifier
              .weight(1f)
              .clickable(onClick = onDismiss),
            shape = RoundedCornerShape(999.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, RavenTheme.colors.bgStyle),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Cancel",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.07).sp,
                color = RavenTheme.colors.subTitle,
              )
            }
          }
          Surface(
            modifier = Modifier
              .weight(1f)
              .clickable {
                onSave(text)
                onDismiss()
              },
            shape = RoundedCornerShape(999.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, RavenTheme.colors.primary),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Save",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.07).sp,
                color = RavenTheme.colors.primary,
              )
            }
          }
        }
      }
    }
  }
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
}
