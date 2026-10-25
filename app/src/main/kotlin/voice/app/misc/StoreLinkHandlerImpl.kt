package voice.app.misc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dev.zacsweers.metro.Inject
import voice.core.common.StoreLinkHandler
import voice.core.logging.api.Logger

@Inject
class StoreLinkHandlerImpl(
  private val context: Context,
) : StoreLinkHandler {
  override fun openRavenListing(): Boolean {
    val marketIntent =
      Intent(
        Intent.ACTION_VIEW,
        "market://details?id=${StoreLinkHandler.RAVEN_PACKAGE}".toUri(),
      )
    if (start(marketIntent)) return true
    val webIntent = Intent(Intent.ACTION_VIEW, StoreLinkHandler.PLAY_LISTING_URL.toUri())
    return start(webIntent)
  }

  private fun start(intent: Intent): Boolean =
    try {
      context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
      true
    } catch (e: ActivityNotFoundException) {
      Logger.w(e, "No activity available for ${intent.data}")
      false
    }
}
