package voice.core.documentfile

interface CachedDocumentFileFactory {
  fun create(uri: String): CachedDocumentFile
}
