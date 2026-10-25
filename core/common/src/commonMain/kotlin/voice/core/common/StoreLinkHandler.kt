package voice.core.common

/**
 * Opens the published Raven store listing. The published package is used even by debug builds so a
 * manual rating action never targets a debug-only application id.
 */
interface StoreLinkHandler {
  fun openRavenListing(): Boolean

  companion object {
    const val RAVEN_PACKAGE: String = "io.github.emeraldrealm.raven"
    const val PLAY_LISTING_URL: String = "https://play.google.com/store/apps/details?id=$RAVEN_PACKAGE"
  }
}
