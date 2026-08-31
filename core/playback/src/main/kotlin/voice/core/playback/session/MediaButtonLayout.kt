package voice.core.playback.session

import android.content.Context
import android.os.Bundle
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import java.util.Locale
import voice.core.strings.R as StringsR

@Inject
class MediaButtonLayout(private val context: Context) {

  fun buttons(speed: Float): List<CommandButton> {
    return listOf(
      // The two side controls move by chapter (mark), matching the in-app player, instead of
      // seeking a fixed number of seconds.
      commandButton(
        command = CustomCommand.ForceSeekToPrevious,
        icon = CommandButton.ICON_PREVIOUS,
        displayName = context.getString(StringsR.string.previous_track),
        slot = CommandButton.SLOT_BACK,
      ),
      commandButton(
        command = CustomCommand.ForceSeekToNext,
        icon = CommandButton.ICON_NEXT,
        displayName = context.getString(StringsR.string.next_track),
        slot = CommandButton.SLOT_FORWARD,
      ),
      commandButton(
        command = CustomCommand.AddBookmark,
        icon = CommandButton.ICON_BOOKMARK_UNFILLED,
        displayName = context.getString(StringsR.string.add_bookmark),
        slot = CommandButton.SLOT_OVERFLOW,
      ),
      commandButton(
        command = CustomCommand.CycleSpeed,
        icon = SpeedCycle.icon(speed),
        displayName = context.getString(StringsR.string.playback_speed) +
          " " + String.format(Locale.getDefault(), "%.1fx", speed),
        slot = CommandButton.SLOT_OVERFLOW,
      ),
    )
  }

  private fun commandButton(
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
