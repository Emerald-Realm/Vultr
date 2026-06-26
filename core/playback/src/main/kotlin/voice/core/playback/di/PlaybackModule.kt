package voice.core.playback.di

import android.content.Context
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionCommand
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import voice.core.featureflag.FeatureFlag
import voice.core.featureflag.Media3AudioOffloadFeatureFlagQualifier
import voice.core.playback.misc.VolumeGain
import voice.core.playback.notification.MainActivityIntentProvider
import voice.core.playback.player.DurationInconsistenciesUpdater
import voice.core.playback.player.OnlyAudioRenderersFactory
import voice.core.playback.player.VoicePlayer
import voice.core.playback.player.onAudioSessionIdChanged
import voice.core.playback.playstate.PlayStateDelegatingListener
import voice.core.playback.playstate.PositionUpdater
import voice.core.playback.session.CustomCommand
import voice.core.playback.session.LibrarySessionCallback
import voice.core.playback.session.PlaybackService
import voice.core.strings.R as StringsR

@ContributesTo(PlaybackScope::class)
interface PlaybackModule {

  @Provides
  @SingleIn(PlaybackScope::class)
  fun mediaSourceFactory(context: Context): MediaSource.Factory {
    val dataSourceFactory = DefaultDataSource.Factory(context)
    val extractorsFactory = DefaultExtractorsFactory()
      .setConstantBitrateSeekingEnabled(true)
    return ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
  }

  @Provides
  @SingleIn(PlaybackScope::class)
  fun player(
    context: Context,
    onlyAudioRenderersFactory: OnlyAudioRenderersFactory,
    mediaSourceFactory: MediaSource.Factory,
    playStateDelegatingListener: PlayStateDelegatingListener,
    positionUpdater: PositionUpdater,
    volumeGain: VolumeGain,
    durationInconsistenciesUpdater: DurationInconsistenciesUpdater,
    @Media3AudioOffloadFeatureFlagQualifier media3AudioOffloadFeatureFlag: FeatureFlag<Boolean>,
  ): Player {
    val audioAttributes = AudioAttributes.Builder()
      .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
      .setUsage(C.USAGE_MEDIA)
      .build()

    return ExoPlayer.Builder(context, onlyAudioRenderersFactory, mediaSourceFactory)
      .setAudioAttributes(audioAttributes, true)
      .setHandleAudioBecomingNoisy(true)
      .setWakeMode(C.WAKE_MODE_LOCAL)
      .build()
      .also { player ->
        if (media3AudioOffloadFeatureFlag.get()) {
          player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(
              TrackSelectionParameters.AudioOffloadPreferences.Builder()
                .setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
                .setIsGaplessSupportRequired(true)
                .setIsSpeedChangeSupportRequired(true)
                .build(),
            )
            .build()
        }
        playStateDelegatingListener.attachTo(player)
        positionUpdater.attachTo(player)
        durationInconsistenciesUpdater.attachTo(player)
        player.onAudioSessionIdChanged {
          volumeGain.audioSessionId = it
        }
      }
  }

  @Provides
  @SingleIn(PlaybackScope::class)
  fun scope(): CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

  @Provides
  @SingleIn(PlaybackScope::class)
  fun session(
    service: PlaybackService,
    player: VoicePlayer,
    callback: LibrarySessionCallback,
    mainActivityIntentProvider: MainActivityIntentProvider,
    context: Context,
  ): MediaLibraryService.MediaLibrarySession {
    return MediaLibraryService.MediaLibrarySession.Builder(service, player, callback)
      .setSessionActivity(mainActivityIntentProvider.toCurrentBook())
      .setMediaButtonPreferences(
        listOf(
          // The two side controls move by chapter (mark), matching the in-app player, instead of
          // seeking a fixed number of seconds.
          chapterCommandButton(
            command = CustomCommand.ForceSeekToPrevious,
            icon = CommandButton.ICON_PREVIOUS,
            displayName = context.getString(StringsR.string.previous_track),
            slot = CommandButton.SLOT_BACK,
          ),
          chapterCommandButton(
            command = CustomCommand.ForceSeekToNext,
            icon = CommandButton.ICON_NEXT,
            displayName = context.getString(StringsR.string.next_track),
            slot = CommandButton.SLOT_FORWARD,
          ),
        ),
      )
      .build()
  }

  private fun chapterCommandButton(
    command: CustomCommand,
    icon: Int,
    displayName: String,
    slot: Int,
  ): CommandButton {
    val extras = Bundle().apply {
      putString(
        CustomCommand.CUSTOM_COMMAND_EXTRA,
        Json.encodeToString(CustomCommand.serializer(), command),
      )
    }
    return CommandButton.Builder(icon)
      .setDisplayName(displayName)
      .setSessionCommand(SessionCommand(CustomCommand.CUSTOM_COMMAND_ACTION, extras))
      .setSlots(slot)
      .build()
  }
}
