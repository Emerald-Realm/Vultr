package voice.core.sleeptimer

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.SleepTimerPreferenceStore
import voice.core.logging.api.Logger
import voice.core.playback.PlayerController
import voice.core.playback.playstate.PlaybackHistoryRecorder
import voice.core.playback.playstate.PlayStateManager
import voice.core.playback.playstate.PlayStateManager.PlayState.Playing
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SleepTimerImpl internal constructor(
  private val playStateManager: PlayStateManager,
  private val shakeDetector: ShakeDetector,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  private val playerController: PlayerController,
  dispatcherProvider: DispatcherProvider,
  private val tracker: SleepTimerTracker,
  private val historyRecorder: PlaybackHistoryRecorder,
) : SleepTimer {

  private val scope = MainScope(dispatcherProvider)
  private val _state = MutableStateFlow<SleepTimerState>(SleepTimerState.Disabled)
  override val state: StateFlow<SleepTimerState> get() = _state

  private var job: Job? = null

  override fun enable(mode: SleepTimerMode) {
    tracker.enabled(mode)
    disable() // cancel any active job first

    job = scope.launch {
      when (mode) {
        is SleepTimerMode.TimedWithDuration -> startCountdown(mode.duration)
        SleepTimerMode.TimedWithDefault -> {
          val pref = sleepTimerPreferenceStore.data.first()
          startCountdown(pref.duration)
        }
        SleepTimerMode.EndOfChapter -> {
          _state.value = SleepTimerState.Enabled.WithEndOfChapter
        }
      }
    }
  }

  override fun disable() {
    tracker.disabled()
    job?.cancel()
    job = null
    _state.value = SleepTimerState.Disabled
    playerController.setVolume(1F)
  }

  private tailrec suspend fun startCountdown(duration: Duration) {
    Logger.d("startCountdown(duration=$duration)")
    var left = duration
    _state.value = SleepTimerState.Enabled.WithDuration(left)

    val interval = 500.milliseconds

    // Volume stays at full until the abrupt pause; no fade-out.
    while (left > Duration.ZERO) {
      suspendUntilPlaying()
      delay(interval)
      left = max((left - interval).inWholeMilliseconds, 0).milliseconds
      _state.value = SleepTimerState.Enabled.WithDuration(left)
    }
    _state.value = SleepTimerState.Disabled

    historyRecorder.onSleepEnded()
    // Plain pause; the rewind is applied uniformly on resume by the player's
    // auto-rewind setting, keeping sleep-timer resume consistent with every other resume.
    playerController.pause()

    val shakeDetected = detectShakeWithTimeout()
    if (shakeDetected) {
      Logger.i("Shake detected, resetting timer")
      playerController.play()
      startCountdown(duration)
    } else {
      suspendUntilPlaying()
      startCountdown(duration)
    }
  }

  private suspend fun detectShakeWithTimeout(): Boolean {
    Logger.d("Waiting $SHAKE_TO_RESET_TIME for shake...")
    return withTimeoutOrNull(SHAKE_TO_RESET_TIME) {
      shakeDetector.detect()
      true
    } ?: false
  }

  private suspend fun suspendUntilPlaying() {
    if (playStateManager.playState != Playing) {
      Logger.i("Not playing. Waiting for playback to continue.")
      playStateManager.flow.first { it == Playing }
      Logger.i("Playback resumed.")
    }
  }

  internal companion object {
    val SHAKE_TO_RESET_TIME = 30.seconds
  }
}
