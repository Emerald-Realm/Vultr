package voice.features.folderPicker.addcontent

import android.net.Uri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import voice.core.data.folders.AudiobookFolders
import voice.core.data.folders.FolderType
import voice.core.data.store.OnboardingCompletedStore
import voice.features.folderPicker.folderPicker.FileTypeSelection
import voice.navigation.Destination
import voice.navigation.Destination.SelectFolderType
import voice.navigation.Navigator
import voice.navigation.Origin

@AssistedInject
class AddContentViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  @Assisted
  private val origin: Origin,
) {

  private val scope = MainScope()

  internal fun add(
    uri: Uri,
    type: FileTypeSelection,
  ) {
    when (type) {
      FileTypeSelection.File -> {
        audiobookFolders.add(uri, FolderType.SingleFile)
        finishOnboardingIfNeeded()
        navigator.setRoot(Destination.BookOverview)
      }
      FileTypeSelection.Folder -> {
        navigator.goTo(
          SelectFolderType(
            uri = uri,
            origin = origin,
          ),
        )
      }
    }
  }

  private fun finishOnboardingIfNeeded() {
    if (origin == Origin.Onboarding) {
      scope.launch { onboardingCompletedStore.updateData { true } }
    }
  }

  internal fun back() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(origin: Origin): AddContentViewModel
  }
}
