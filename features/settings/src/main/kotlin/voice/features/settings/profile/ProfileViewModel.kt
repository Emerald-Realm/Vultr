package voice.features.settings.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.common.MainScope
import voice.core.data.Book
import voice.core.data.ReaderProfile
import voice.core.data.repo.BookRepository
import voice.core.data.store.ReaderProfileStore
import voice.navigation.Navigator
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Inject
class ProfileViewModel(
  @ReaderProfileStore private val profileStore: DataStore<ReaderProfile>,
  bookRepository: BookRepository,
  private val navigator: Navigator,
  dispatcherProvider: DispatcherProvider,
) {
  private val mainScope = MainScope(dispatcherProvider)
  private val state = combine(profileStore.data, bookRepository.flow()) { profile, books ->
    profile.toViewState(books)
  }

  @Composable
  fun viewState(): ProfileViewState {
    val value by remember { state }.collectAsState(ProfileViewState())
    return value
  }

  fun close() = navigator.goBack()

  fun updateName(name: String) {
    val cleaned = name.trim().take(40)
    if (cleaned.isEmpty()) return
    mainScope.launch {
      profileStore.updateData { it.copy(name = cleaned) }
    }
  }

  fun updateImage(uri: String?) {
    mainScope.launch {
      profileStore.updateData { it.copy(imageUri = uri) }
    }
  }
}

data class ProfileViewState(
  val name: String = ReaderProfile.DEFAULT_NAME,
  val imageUri: String? = null,
  val completedBooks: Int = 0,
  val level: Int = 0,
  val booksIntoLevel: Int = 0,
  val listenedHours: Long = 0,
)

private fun ReaderProfile.toViewState(books: List<Book>): ProfileViewState {
  val completed = books.count { it.isCompleted }
  val listenedMs = books.sumOf { it.position.coerceIn(0L, it.duration) }
  return ProfileViewState(
    name = name,
    imageUri = imageUri,
    completedBooks = completed,
    level = completed / BOOKS_PER_LEVEL,
    booksIntoLevel = completed % BOOKS_PER_LEVEL,
    listenedHours = (listenedMs.toDouble() / TimeUnit.HOURS.toMillis(1)).roundToLong(),
  )
}

private val Book.isCompleted: Boolean
  get() = duration > 0L && position >= duration - TimeUnit.SECONDS.toMillis(5)

internal const val BOOKS_PER_LEVEL = 5
