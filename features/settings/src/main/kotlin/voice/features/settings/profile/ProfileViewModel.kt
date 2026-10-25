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
import voice.core.data.ReaderProfile
import voice.core.data.listening.ListeningStats
import voice.core.data.repo.ListeningStatsRepo
import voice.core.data.store.ReaderProfileStore
import voice.navigation.Navigator
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@Inject
class ProfileViewModel(
  @ReaderProfileStore private val profileStore: DataStore<ReaderProfile>,
  listeningStatsRepo: ListeningStatsRepo,
  private val navigator: Navigator,
  dispatcherProvider: DispatcherProvider,
) {
  private val mainScope = MainScope(dispatcherProvider)
  private val state = combine(profileStore.data, listeningStatsRepo.stats) { profile, stats ->
    profile.toViewState(stats)
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
  val barFraction: Float = 0f,
  val listenedHours: Long = 0,
)

internal fun ReaderProfile.toViewState(stats: ListeningStats): ProfileViewState {
  val completedBooks = stats.awardedBookIds.size
  val booksIntoLevel = completedBooks % BOOKS_PER_LEVEL
  return ProfileViewState(
    name = name,
    imageUri = imageUri,
    completedBooks = completedBooks,
    level = completedBooks / BOOKS_PER_LEVEL,
    booksIntoLevel = booksIntoLevel,
    barFraction = booksIntoLevel / BOOKS_PER_LEVEL.toFloat(),
    listenedHours = (stats.lifetimeListenedMs.toDouble() / TimeUnit.HOURS.toMillis(1)).roundToLong(),
  )
}

internal const val BOOKS_PER_LEVEL = 5
