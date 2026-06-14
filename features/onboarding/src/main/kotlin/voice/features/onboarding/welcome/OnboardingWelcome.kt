package voice.features.onboarding.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voice.core.common.rootGraphAs
import voice.core.ui.VoiceTheme
import voice.core.ui.R as UiR
import voice.core.strings.R as StringsR

@Composable
fun OnboardingWelcome(modifier: Modifier = Modifier) {
  val viewModel = retain<OnboardingWelcomeViewModel> {
    rootGraphAs<OnboardingWelcomeProvider>()
      .onboardingWelcomeViewModel
  }
  OnboardingWelcome(modifier = modifier, onNext = viewModel::next)
}

@Composable
private fun OnboardingWelcome(
  onNext: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(id = UiR.drawable.ic_raven_logo),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(width = 154.dp, height = 185.dp),
        )
      }

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(StringsR.string.onboarding_welcome_title),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.displaySmall,
        fontSize = 36.sp,
        lineHeight = 39.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp),
        text = stringResource(StringsR.string.onboarding_welcome_subtitle),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 26.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Button(
          onClick = onNext,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        ) {
          Text(
            text = stringResource(StringsR.string.onboarding_welcome_get_started),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
          text = stringResource(StringsR.string.onboarding_welcome_terms),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodyMedium,
          lineHeight = 22.sp,
          color = MaterialTheme.colorScheme.outline,
        )
      }
    }
  }
}

@Composable
@Preview
private fun OnboardingWelcomePreview() {
  VoiceTheme {
    OnboardingWelcome(
      onNext = {},
    )
  }
}
