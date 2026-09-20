package voice.core.documentfile

import android.content.Context
import android.net.Uri

internal data class RealCachedDocumentFile(
  val context: Context,
  private val androidUri: Uri,
  private val preFilledContent: FileContents?,
) : CachedDocumentFile {

  override val uri: String = androidUri.toString()

  override val children: List<CachedDocumentFile> by lazy {
    if (isDirectory) {
      parseContents(androidUri, context)
    } else {
      emptyList()
    }
  }

  private val content: FileContents? by lazy {
    preFilledContent ?: FileContents.query(context, androidUri)
  }

  override val name: String? by lazy { content?.name }
  override val isDirectory: Boolean by lazy { content?.isDirectory ?: false }
  override val isFile: Boolean by lazy { content?.isFile ?: false }
  override val length: Long by lazy { content?.length ?: 0L }
  override val lastModified: Long by lazy { content?.lastModified ?: 0L }

  override fun toString(): String = "RealCachedDocumentFile($androidUri)"
}
