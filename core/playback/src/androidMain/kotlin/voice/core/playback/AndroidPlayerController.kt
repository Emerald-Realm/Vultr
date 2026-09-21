package voice.core.playback

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.ChapterId
import voice.core.data.repo.BookRepository
import voice.core.data.store.CurrentBookStore
import voice.core.logging.api.Logger
import voice.core.playback.misc.Decibel
import voice.core.playback.session.CustomCommand
import voice.core.playback.session.MediaId
import voice.core.playback.session.MediaItemProvider
import voice.core.playback.session.PlaybackService
import voice.core.playback.session.sendCustomCommand
import voice.core.playback.session.toMediaIdOrNull
import kotlin.time.Duration.Companion.milliseconds

@ContributesBinding(AppScope::class)
class AndroidPlayerController(
  private val context: Context,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
  private val bookRepository: BookRepository,
  private val mediaItemProvider: MediaItemProvider,
) : PlayerController {

  private var _controller: Deferred<MediaController> = newControllerAsync()

  private fun newControllerAsync() = MediaController
    .Builder(context, SessionToken(context, ComponentName(context, PlaybackService::class.java)))
    .buildAsync()
    .asDeferred()

  private val controller: Deferred<MediaController>
    get() {
      if (_controller.isCompleted) {
        val completedController = _controller.getCompleted()
        if (!completedController.isConnected) {
          completedController.release()
          _controller = newControllerAsync()
        }
      }
      return _controller
    }
  private val scope = CoroutineScope(Dispatchers.Main.immediate)

  override fun setPosition(
    time: Long,
    id: ChapterId,
  ) = executeAfterPrepare { controller ->
    val bookId = currentBookStoreId.data.first() ?: return@executeAfterPrepare
    val book = bookRepository.get(bookId) ?: return@executeAfterPrepare
    val index = book.chapters.indexOfFirst { it.id == id }
    if (index != -1) {
      controller.seekTo(index, time)
    }
  }

  override fun pauseIfCurrentBookDifferentFrom(id: BookId) {
    scope.launch {
      val controller = awaitConnect() ?: return@launch
      val currentBookId = controller.currentBookId()
      if (currentBookId != null && currentBookId != id) {
        controller.pause()
      }
    }
  }

  override fun skipSilence(skip: Boolean) = executeAfterPrepare { controller ->
    controller.sendCustomCommand(CustomCommand.SetSkipSilence(skip))
  }

  override fun fastForward() = executeAfterPrepare { controller ->
    controller.seekForward()
  }

  override fun rewind() = executeAfterPrepare { controller ->
    controller.seekBack()
  }

  override fun previous() = executeAfterPrepare { controller ->
    controller.sendCustomCommand(CustomCommand.ForceSeekToPrevious)
  }

  override fun next() = executeAfterPrepare { controller ->
    controller.sendCustomCommand(CustomCommand.ForceSeekToNext)
  }

  override fun play() = executeAfterPrepare { controller ->
    controller.play()
  }

  /**
   * Warms up the player for the current book without starting playback so that a
   * subsequent [play] resumes instantly. Safe to call repeatedly: [maybePrepare]
   * is a no-op once the current book is already prepared.
   */
  override fun prepare() {
    scope.launch {
      val controller = awaitConnect() ?: return@launch
      maybePrepare(controller)
    }
  }

  override fun playPause() = executeAfterPrepare { controller ->
    if (controller.isPlaying) {
      controller.pause()
    } else {
      controller.play()
    }
  }

  @IgnorableReturnValue
  private suspend fun maybePrepare(controller: MediaController): Boolean {
    val bookId = currentBookStoreId.data.first() ?: return false
    if (controller.currentBookId() == bookId &&
      controller.playbackState in listOf(Player.STATE_READY, Player.STATE_BUFFERING)
    ) {
      return true
    }
    val book = bookRepository.get(bookId) ?: return false
    controller.setMediaItem(mediaItemProvider.mediaItem(book))
    controller.prepare()
    return true
  }

  private fun MediaController.currentBookId(): BookId? {
    val currentMediaItem = currentMediaItem ?: return null
    val mediaId = currentMediaItem.mediaId.toMediaIdOrNull() ?: return null
    return when (mediaId) {
      is MediaId.Book -> mediaId.id
      is MediaId.Chapter -> mediaId.bookId
      MediaId.Recent -> null
      MediaId.Root -> null
    }
  }

  override fun pause() = executeAfterPrepare { controller ->
    controller.pause()
  }

  override fun setSpeed(speed: Float) = executeAfterPrepare { controller ->
    controller.setPlaybackSpeed(speed)
  }

  override fun setGain(gain: Decibel) = executeAfterPrepare { controller ->
    controller.sendCustomCommand(CustomCommand.SetGain(gain))
  }

  override fun setVolume(volume: Float) = executeAfterConnect {
    require(volume in 0F..1F)
    it.volume = volume
  }

  override suspend fun livePlaybackState(bookId: BookId?): LivePlaybackState? {
    val controller = awaitConnect() ?: return null
    return controller.livePlaybackStateSnapshot(bookId)
  }

  override fun livePlaybackStateFlow(bookId: BookId?): Flow<LivePlaybackState?> = callbackFlow {
    val controller = awaitConnect()
    if (controller == null) {
      trySend(null)
      close()
      return@callbackFlow
    }

    fun emitSnapshot() {
      trySend(controller.livePlaybackStateSnapshot(bookId))
    }

    var tickJob: Job? = null
    fun updateTicking() {
      if (!controller.isPlaying) {
        tickJob?.cancel()
        return
      }
      if (tickJob?.isActive == true) {
        return
      }
      tickJob = launch {
        while (isActive) {
          delay(250.milliseconds)
          emitSnapshot()
        }
      }
    }

    val listener = object : Player.Listener {
      override fun onEvents(
        player: Player,
        events: Player.Events,
      ) {
        if (events.containsAny(
            Player.EVENT_PLAY_WHEN_READY_CHANGED,
            Player.EVENT_MEDIA_ITEM_TRANSITION,
            Player.EVENT_PLAYBACK_STATE_CHANGED,
          )
        ) {
          emitSnapshot()
          updateTicking()
        }
        if (events.containsAny(
            Player.EVENT_POSITION_DISCONTINUITY,
            Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
          )
        ) {
          emitSnapshot()
        }
      }
    }

    controller.addListener(listener)
    emitSnapshot()
    updateTicking()
    awaitClose {
      tickJob?.cancel()
      controller.removeListener(listener)
    }
  }

  private inline fun executeAfterPrepare(crossinline action: suspend (MediaController) -> Unit) {
    scope.launch {
      val controller = awaitConnect() ?: return@launch
      if (maybePrepare(controller)) {
        action(controller)
      }
    }
  }

  private inline fun executeAfterConnect(crossinline action: suspend (MediaController) -> Unit) {
    scope.launch {
      val controller = awaitConnect() ?: return@launch
      action(controller)
    }
  }

  @IgnorableReturnValue
  override suspend fun awaitReady() {
    if (awaitConnect() == null) {
      Logger.w("Could not connect to the media controller")
    }
  }

  private suspend fun awaitConnect(): MediaController? {
    return try {
      controller.await()
    } catch (e: Exception) {
      if (e is CancellationException) currentCoroutineContext().ensureActive()
      Logger.w(e, "Error while connecting to media controller")
      null
    }
  }
}
