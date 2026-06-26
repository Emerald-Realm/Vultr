package voice.core.playback.session

import android.content.Context
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.google.common.collect.ImmutableList
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import voice.core.playback.R

@Inject
class VoiceMediaNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {

  init {
    // Use the Raven mark as the status-bar / cover-badge icon instead of the generic media glyph.
    setSmallIcon(R.drawable.ic_raven_notification)
  }

  override fun getMediaButtons(
    session: MediaSession,
    playerCommands: Player.Commands,
    customLayout: ImmutableList<CommandButton>,
    showPauseButton: Boolean,
  ): ImmutableList<CommandButton> {
    val defaults = super.getMediaButtons(session, playerCommands, customLayout, showPauseButton)
    val playPause = defaults.firstOrNull { it.playerCommand == Player.COMMAND_PLAY_PAUSE }

    // Previous / next move by chapter (mark), matching the in-app player, instead of the
    // default seek-to-previous/next which only jumps a fixed number of seconds.
    val buttons = listOfNotNull(
      chapterButton(
        command = CustomCommand.ForceSeekToPrevious,
        icon = CommandButton.ICON_PREVIOUS,
        iconRes = R.drawable.ic_skip_to_previous,
        displayName = "Previous chapter",
      ),
      playPause,
      chapterButton(
        command = CustomCommand.ForceSeekToNext,
        icon = CommandButton.ICON_NEXT,
        iconRes = R.drawable.ic_skip_to_next,
        displayName = "Next chapter",
      ),
    )

    buttons.forEachIndexed { index, button ->
      // This shows the buttons in compact mode for Android < 13
      // https://github.com/VoiceAudiobook/Voice/issues/1904
      button.extras.putInt(COMMAND_KEY_COMPACT_VIEW_INDEX, index)
    }
    return ImmutableList.copyOf(buttons)
  }

  private fun chapterButton(
    command: CustomCommand,
    icon: Int,
    iconRes: Int,
    displayName: String,
  ): CommandButton {
    val extras = Bundle().apply {
      putString(
        CustomCommand.CUSTOM_COMMAND_EXTRA,
        Json.encodeToString(CustomCommand.serializer(), command),
      )
    }
    return CommandButton.Builder(icon)
      .setDisplayName(displayName)
      .setCustomIconResId(iconRes)
      .setSessionCommand(SessionCommand(CustomCommand.CUSTOM_COMMAND_ACTION, extras))
      .build()
  }
}
