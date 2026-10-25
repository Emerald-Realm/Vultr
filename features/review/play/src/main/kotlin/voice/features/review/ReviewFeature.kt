package voice.features.review

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import voice.core.common.StoreLinkHandler
import voice.core.common.rootGraphAs
import voice.core.data.repo.ListeningStatsRepo
import voice.core.logging.api.Logger
import voice.core.playback.playstate.PlayStateManager
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

@ContributesTo(AppScope::class)
interface RatingReminderGraph {
  val ratingReminderCoordinator: RatingReminderCoordinator
  val listeningStatsRepo: ListeningStatsRepo
  val playStateManager: PlayStateManager
  val storeLinkHandler: StoreLinkHandler
}

@Composable
fun ReviewFeature(automaticReminderAllowed: Boolean) {
  val graph = rootGraphAs<RatingReminderGraph>()
  val stats by graph.listeningStatsRepo.stats
    .collectAsState(initial = null)
  val playState by graph.playStateManager.flow.collectAsState()
  val isResumed = rememberActivityResumed()
  val today by rememberToday()

  var showReminder by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(automaticReminderAllowed, isResumed, playState, stats, today) {
    val currentStats = stats ?: return@LaunchedEffect
    if (!automaticReminderAllowed || !isResumed) return@LaunchedEffect
    if (playState == PlayStateManager.PlayState.Playing) return@LaunchedEffect
    delay(SAFE_MOMENT_DELAY_MS)
    if (graph.ratingReminderCoordinator.tryPresent(today, currentStats)) {
      showReminder = true
    }
  }

  if (showReminder) {
    RatingReminderDialog(
      onRate = {
        showReminder = false
        if (!graph.storeLinkHandler.openRavenListing()) {
          Logger.w("Couldn't open the Play Store listing for a rating")
        }
      },
      onNotNow = { showReminder = false },
      onAlreadyRated = {
        showReminder = false
        scope.launch { graph.ratingReminderCoordinator.markAlreadyRated() }
      },
      onDismiss = { showReminder = false },
    )
  }
}

@Composable
private fun rememberActivityResumed(): Boolean {
  val activity = LocalActivity.current
  val lifecycle = (activity as? LifecycleOwner)?.lifecycle
  var resumed by remember(lifecycle) {
    mutableStateOf(lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true)
  }
  DisposableEffect(lifecycle) {
    if (lifecycle == null) return@DisposableEffect onDispose {}
    val observer =
      LifecycleEventObserver { _, _ ->
        resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
      }
    lifecycle.addObserver(observer)
    onDispose { lifecycle.removeObserver(observer) }
  }
  return resumed
}

@Composable
private fun rememberToday(): androidx.compose.runtime.State<LocalDate> =
  produceState(initialValue = LocalDate.now()) {
    while (true) {
      val now = ZonedDateTime.now()
      val tomorrow = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
      value = now.toLocalDate()
      delay(Duration.between(now, tomorrow).toMillis().coerceAtLeast(MIN_DATE_TICK_MS) + DATE_TICK_SLACK_MS)
    }
  }

private const val SAFE_MOMENT_DELAY_MS = 2_000L
private const val MIN_DATE_TICK_MS = 1_000L
private const val DATE_TICK_SLACK_MS = 1_000L
