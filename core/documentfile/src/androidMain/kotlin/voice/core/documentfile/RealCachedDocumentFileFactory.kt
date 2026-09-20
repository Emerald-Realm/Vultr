package voice.core.documentfile

import android.content.Context
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class RealCachedDocumentFileFactory(private val context: Context) : CachedDocumentFileFactory {
  override fun create(uri: String): CachedDocumentFile {
    return RealCachedDocumentFile(context = context, androidUri = uri.toUri(), preFilledContent = null)
  }
}
