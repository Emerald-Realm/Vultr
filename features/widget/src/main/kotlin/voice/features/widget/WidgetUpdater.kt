package voice.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.core.DataStore
import coil.imageLoader
import coil.request.ImageRequest
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.app.features.widget.BaseWidgetProvider
import voice.app.features.widget.RavenWidgetBarProvider
import voice.app.features.widget.RavenWidgetCompactProvider
import voice.app.features.widget.RavenWidgetControlsProvider
import voice.app.features.widget.RavenWidgetShowcaseProvider
import voice.core.data.Book
import voice.core.data.BookId
import voice.core.data.repo.BookRepository
import voice.core.data.store.CurrentBookStore
import voice.core.playback.notification.MainActivityIntentProvider
import voice.core.playback.playstate.PlayStateManager
import voice.core.playback.receiver.WidgetButtonReceiver
import voice.core.ui.dpToPxRounded
import voice.core.ui.R as UiR

@SingleIn(AppScope::class)
@Inject
class WidgetUpdater(
  private val context: Context,
  private val repo: BookRepository,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val playStateManager: PlayStateManager,
  private val mainActivityIntentProvider: MainActivityIntentProvider,
) {

  private val appWidgetManager = AppWidgetManager.getInstance(context)

  private val scope = CoroutineScope(Dispatchers.IO)

  fun update() {
    scope.launch {
      val book = currentBookStore.data.first()?.let {
        repo.get(it)
      }
      updateLegacyWidget(book)
      updateFixedWidget(book, RavenWidgetCompactProvider::class.java, R.layout.widget_compact, ::populateCompact)
      updateFixedWidget(book, RavenWidgetBarProvider::class.java, R.layout.widget_bar, ::populateBar)
      updateFixedWidget(book, RavenWidgetControlsProvider::class.java, R.layout.widget_controls, ::populateControls)
      updateFixedWidget(book, RavenWidgetShowcaseProvider::class.java, R.layout.widget_showcase, ::populateShowcase)
    }
  }

  private suspend fun updateFixedWidget(
    book: Book?,
    provider: Class<out AppWidgetProvider>,
    layout: Int,
    populate: suspend (RemoteViews, Book?) -> Unit,
  ) {
    val componentName = ComponentName(context, provider)
    val ids = appWidgetManager.getAppWidgetIds(componentName)
    for (widgetId in ids) {
      val remoteViews = RemoteViews(context.packageName, layout)
      populate(remoteViews, book)
      appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }
  }

  private suspend fun populateCompact(
    remoteViews: RemoteViews,
    book: Book?,
  ) {
    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, openCurrentBookIntent())
    if (book == null) {
      remoteViews.setImageViewResource(R.id.cover, UiR.drawable.album_art)
      return
    }
    remoteViews.setTextViewText(R.id.title, book.content.name)
    remoteViews.setTextViewText(R.id.timeLeft, timeLeftText(book))
    setCover(remoteViews, book, COVER_SMALL_PX)
  }

  private suspend fun populateBar(
    remoteViews: RemoteViews,
    book: Book?,
  ) {
    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, openCurrentBookIntent())
    bindButton(remoteViews, R.id.playPause, WidgetButtonReceiver.Action.PlayPause)
    bindButton(remoteViews, R.id.rewind, WidgetButtonReceiver.Action.Rewind)
    bindButton(remoteViews, R.id.skipNext, WidgetButtonReceiver.Action.SkipToNext)
    remoteViews.setImageViewResource(R.id.playPause, playPauseIcon())
    if (book == null) return
    remoteViews.setTextViewText(R.id.title, book.content.name)
    remoteViews.setTextViewText(R.id.author, subtitle(book))
  }

  private suspend fun populateControls(
    remoteViews: RemoteViews,
    book: Book?,
  ) {
    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, openCurrentBookIntent())
    bindButton(remoteViews, R.id.rewind, WidgetButtonReceiver.Action.Rewind)
    bindButton(remoteViews, R.id.skipPrevious, WidgetButtonReceiver.Action.SkipToPrevious)
    bindButton(remoteViews, R.id.playPause, WidgetButtonReceiver.Action.PlayPause)
    bindButton(remoteViews, R.id.skipNext, WidgetButtonReceiver.Action.SkipToNext)
    bindButton(remoteViews, R.id.fastForward, WidgetButtonReceiver.Action.FastForward)
    remoteViews.setImageViewResource(R.id.playPause, playPauseIcon())
    if (book == null) {
      remoteViews.setImageViewResource(R.id.cover, UiR.drawable.album_art)
      return
    }
    remoteViews.setTextViewText(R.id.title, book.content.name)
    remoteViews.setTextViewText(R.id.author, subtitle(book))
    setCover(remoteViews, book, COVER_SMALL_PX)
  }

  private suspend fun populateShowcase(
    remoteViews: RemoteViews,
    book: Book?,
  ) {
    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, openCurrentBookIntent())
    bindButton(remoteViews, R.id.playPause, WidgetButtonReceiver.Action.PlayPause)
    remoteViews.setImageViewResource(R.id.playPause, playPauseIcon())
    if (book == null) {
      remoteViews.setImageViewResource(R.id.cover, UiR.drawable.album_art)
      return
    }
    remoteViews.setTextViewText(R.id.title, book.content.name)
    remoteViews.setTextViewText(R.id.author, book.content.author.orEmpty())
    remoteViews.setTextViewText(R.id.timeLeft, timeLeftText(book))
    setCover(remoteViews, book, COVER_LARGE_PX)
  }

  private fun bindButton(
    remoteViews: RemoteViews,
    viewId: Int,
    action: WidgetButtonReceiver.Action,
  ) {
    remoteViews.setOnClickPendingIntent(viewId, WidgetButtonReceiver.pendingIntent(context, action))
  }

  private fun playPauseIcon(): Int {
    return if (playStateManager.playState == PlayStateManager.PlayState.Playing) {
      UiR.drawable.ic_pause_white_36dp
    } else {
      UiR.drawable.ic_play_white_36dp
    }
  }

  private fun openCurrentBookIntent(): PendingIntent {
    return mainActivityIntentProvider.toCurrentBook()
  }

  private fun subtitle(book: Book): String {
    return book.content.author ?: book.currentChapter.name ?: book.content.name
  }

  private fun timeLeftText(book: Book): String {
    val remainingMs = (book.duration - book.position).coerceAtLeast(0)
    val totalMinutes = remainingMs / 60_000L
    val hours = (totalMinutes / 60L).toInt()
    val minutes = (totalMinutes % 60L).toInt()
    return if (hours > 0) {
      context.getString(R.string.widget_time_left_hm, hours, minutes)
    } else {
      context.getString(R.string.widget_time_left_m, minutes)
    }
  }

  private suspend fun setCover(
    remoteViews: RemoteViews,
    book: Book,
    sizePx: Int,
  ) {
    val bitmap = loadCover(book, sizePx)
    if (bitmap != null) {
      remoteViews.setImageViewBitmap(R.id.cover, bitmap)
    } else {
      remoteViews.setImageViewResource(R.id.cover, UiR.drawable.album_art)
    }
  }

  private suspend fun loadCover(
    book: Book,
    sizePx: Int,
  ): Bitmap? {
    val coverFile = book.content.cover ?: return null
    if (sizePx <= 0) return null
    return runCatching {
      context.imageLoader
        .execute(
          ImageRequest.Builder(context)
            .data(coverFile)
            .size(sizePx, sizePx)
            .fallback(UiR.drawable.album_art)
            .error(UiR.drawable.album_art)
            .allowHardware(false)
            .build(),
        )
        .drawable
        ?.toBitmap()
    }.getOrNull()
  }

  // --- Legacy resizable widget (unchanged behavior) ---

  private suspend fun updateLegacyWidget(book: Book?) {
    val componentName = ComponentName(context, BaseWidgetProvider::class.java)
    val ids = appWidgetManager.getAppWidgetIds(componentName)
    for (widgetId in ids) {
      if (book != null) {
        initWidgetForPresentBook(widgetId, book)
      } else {
        initWidgetForAbsentBook(widgetId)
      }
    }
  }

  private suspend fun initWidgetForPresentBook(
    widgetId: Int,
    book: Book,
  ) {
    val opts = appWidgetManager.getAppWidgetOptions(widgetId)
    val useWidth = widgetWidth(opts)
    val useHeight = widgetHeight(opts)

    val remoteViews = RemoteViews(context.packageName, R.layout.widget)
    initElements(remoteViews = remoteViews, book = book, coverSize = useHeight)

    if (useWidth > 0 && useHeight > 0) {
      setVisibilities(remoteViews, useWidth, useHeight, book.content.chapters.size == 1)
    }
    appWidgetManager.updateAppWidget(widgetId, remoteViews)
  }

  private fun widgetWidth(opts: Bundle): Int {
    val key = if (isPortrait) {
      AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
    } else {
      AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
    }
    val dp = opts.getInt(key)
    return context.dpToPxRounded(dp.toFloat())
  }

  private fun widgetHeight(opts: Bundle): Int {
    val key = if (isPortrait) {
      AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
    } else {
      AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT
    }
    val dp = opts.getInt(key)
    return context.dpToPxRounded(dp.toFloat())
  }

  private fun initWidgetForAbsentBook(widgetId: Int) {
    val remoteViews = RemoteViews(context.packageName, R.layout.widget)
    val wholeWidgetClickPI = mainActivityIntentProvider.toCurrentBook()
    remoteViews.setImageViewResource(R.id.imageView, UiR.drawable.album_art)
    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, wholeWidgetClickPI)
    appWidgetManager.updateAppWidget(widgetId, remoteViews)
  }

  private val isPortrait: Boolean
    get() {
      val orientation = context.resources.configuration.orientation
      return orientation == Configuration.ORIENTATION_PORTRAIT
    }

  private suspend fun initElements(
    remoteViews: RemoteViews,
    book: Book,
    coverSize: Int,
  ) {
    val playPausePI = WidgetButtonReceiver.pendingIntent(context, WidgetButtonReceiver.Action.PlayPause)
    remoteViews.setOnClickPendingIntent(R.id.playPause, playPausePI)

    val fastForwardPI = WidgetButtonReceiver.pendingIntent(context, WidgetButtonReceiver.Action.FastForward)
    remoteViews.setOnClickPendingIntent(R.id.fastForward, fastForwardPI)

    val rewindPI = WidgetButtonReceiver.pendingIntent(context, WidgetButtonReceiver.Action.Rewind)
    remoteViews.setOnClickPendingIntent(R.id.rewind, rewindPI)

    remoteViews.setImageViewResource(R.id.playPause, playPauseIcon())

    // if we have any book, init the views and have a click on the whole widget start BookPlay.
    // if we have no book, simply have a click on the whole widget start BookChoose.
    remoteViews.setTextViewText(R.id.title, book.content.name)
    val name = book.currentChapter.name

    remoteViews.setTextViewText(R.id.summary, name)

    val wholeWidgetClickPI = mainActivityIntentProvider.toCurrentBook()

    val coverFile = book.content.cover
    if (coverFile != null && coverSize > 0) {
      val bitmap = context.imageLoader
        .execute(
          ImageRequest.Builder(context)
            .data(coverFile)
            .size(coverSize, coverSize)
            .fallback(UiR.drawable.album_art)
            .error(UiR.drawable.album_art)
            .allowHardware(false)
            .build(),
        )
        .drawable!!.toBitmap()
      remoteViews.setImageViewBitmap(R.id.imageView, bitmap)
    } else {
      remoteViews.setImageViewResource(R.id.imageView, UiR.drawable.album_art)
    }

    remoteViews.setOnClickPendingIntent(R.id.wholeWidget, wholeWidgetClickPI)
  }

  private fun setVisibilities(
    remoteViews: RemoteViews,
    width: Int,
    height: Int,
    singleChapter: Boolean,
  ) {
    setHorizontalVisibility(remoteViews, width, height)
    setVerticalVisibility(remoteViews, height, singleChapter)
  }

  private fun setHorizontalVisibility(
    remoteViews: RemoteViews,
    widgetWidth: Int,
    coverSize: Int,
  ) {
    val singleButtonSize = context.dpToPxRounded(8F + 36F + 8F)
    // widget height because cover is square
    var summarizedItemWidth = 3 * singleButtonSize + coverSize

    // set all views visible
    remoteViews.setViewVisibility(R.id.imageView, View.VISIBLE)
    remoteViews.setViewVisibility(R.id.rewind, View.VISIBLE)
    remoteViews.setViewVisibility(R.id.fastForward, View.VISIBLE)

    // hide cover if we need space
    if (summarizedItemWidth > widgetWidth) {
      remoteViews.setViewVisibility(R.id.imageView, View.GONE)
      summarizedItemWidth -= coverSize
    }

    // hide fast forward if we need space
    if (summarizedItemWidth > widgetWidth) {
      remoteViews.setViewVisibility(R.id.fastForward, View.GONE)
      summarizedItemWidth -= singleButtonSize
    }

    // hide rewind if we need space
    if (summarizedItemWidth > widgetWidth) {
      remoteViews.setViewVisibility(R.id.rewind, View.GONE)
    }
  }

  private fun setVerticalVisibility(
    remoteViews: RemoteViews,
    widgetHeight: Int,
    singleChapter: Boolean,
  ) {
    val buttonSize = context.dpToPxRounded(8F + 36F + 8F)
    val titleSize = context.resources.getDimensionPixelSize(R.dimen.list_text_primary_size)
    val summarySize = context.resources.getDimensionPixelSize(R.dimen.list_text_secondary_size)

    var summarizedItemsHeight = buttonSize + titleSize + summarySize

    // first setting all views visible
    remoteViews.setViewVisibility(R.id.summary, View.VISIBLE)
    remoteViews.setViewVisibility(R.id.title, View.VISIBLE)

    // when we are in a single chapter or we are to high, hide summary
    if (singleChapter || widgetHeight < summarizedItemsHeight) {
      remoteViews.setViewVisibility(R.id.summary, View.GONE)
      summarizedItemsHeight -= summarySize
    }

    // if we ar still to high, hide title
    if (summarizedItemsHeight > widgetHeight) {
      remoteViews.setViewVisibility(R.id.title, View.GONE)
    }
  }

  private companion object {
    const val COVER_SMALL_PX = 160
    const val COVER_LARGE_PX = 320
  }
}
