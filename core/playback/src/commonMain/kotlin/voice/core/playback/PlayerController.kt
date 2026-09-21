package voice.core.playback

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.ChapterId
import voice.core.playback.misc.Decibel

/**
 * Platform-neutral playback control surface. The Android implementation is backed by Media3;
 * a future Apple implementation would be backed by AVPlayer.
 */
interface PlayerController {

  fun setPosition(
    time: Long,
    id: ChapterId,
  )

  fun pauseIfCurrentBookDifferentFrom(id: BookId)

  fun skipSilence(skip: Boolean)

  fun fastForward()

  fun rewind()

  fun previous()

  fun next()

  fun play()

  fun prepare()

  fun playPause()

  fun pause()

  fun setSpeed(speed: Float)

  fun setGain(gain: Decibel)

  fun setVolume(volume: Float)

  suspend fun livePlaybackState(bookId: BookId? = null): LivePlaybackState?

  fun livePlaybackStateFlow(bookId: BookId? = null): Flow<LivePlaybackState?>

  /** Suspends until the underlying player is connected, or the connection fails. */
  suspend fun awaitReady()
}
