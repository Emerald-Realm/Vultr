package voice.core.scanner

import dev.zacsweers.metro.Inject
import voice.core.data.BookId
import voice.core.data.audioFileCount
import voice.core.data.folders.FolderType
import voice.core.data.isAudioFile
import voice.core.data.repo.BookContentRepo
import voice.core.documentfile.CachedDocumentFile
import voice.core.documentfile.walk
import voice.core.logging.api.Logger

@Inject
internal class MediaScanner(
  private val contentRepo: BookContentRepo,
  private val chapterParser: ChapterParser,
  private val bookParser: BookParser,
  private val deviceHasPermissionBug: DeviceHasStoragePermissionBug,
) {

  suspend fun scan(folders: Map<FolderType, List<CachedDocumentFile>>) {
    val files = folders.flatMap { (folderType, files) ->
      when (folderType) {
        FolderType.SingleFile, FolderType.SingleFolder -> {
          files
        }
        FolderType.Root -> {
          // Each child is resolved into one or more whole books, so nested
          // structures (author → book, series → book) and multi-disc books are
          // recognized correctly instead of being merged or split blindly.
          files.flatMap { root ->
            root.children.flatMap { child -> child.bookRoots() }
          }
        }
        FolderType.Author -> {
          files.flatMap { folder ->
            folder.children.flatMap { author ->
              if (author.isFile) {
                listOf(author)
              } else {
                author.children
              }
            }
          }
        }
      }
    }

    contentRepo.setAllInactiveExcept(files.map { BookId(it.uri) })

    val probeFile = folders.values.flatten().findProbeFile()
    if (probeFile != null) {
      if (deviceHasPermissionBug.checkForBugAndSet(probeFile)) {
        Logger.w("Device has permission bug, aborting scan! Probed $probeFile")
        return
      }
    }

    files
      .sortedBy { it.audioFileCount() }
      .forEach { file ->
        scan(file)
      }
  }

  private fun List<CachedDocumentFile>.findProbeFile(): CachedDocumentFile? {
    return asSequence().flatMap { it.walk() }
      .firstOrNull { child ->
        child.isAudioFile() && child.uri.authority == "com.android.externalstorage.documents"
      }
  }

  private suspend fun scan(file: CachedDocumentFile) {
    val chapters = chapterParser.parse(file)
    if (chapters.isEmpty()) return

    val content = bookParser.parseAndStore(chapters, file)

    val chapterIds = chapters.map { it.id }
    val currentChapterGone = content.currentChapter !in chapterIds
    val currentChapter = if (currentChapterGone) chapterIds.first() else content.currentChapter
    val positionInChapter = if (currentChapterGone) 0 else content.positionInChapter
    val updated = content.copy(
      chapters = chapterIds,
      currentChapter = currentChapter,
      positionInChapter = positionInChapter,
      isActive = true,
    )
    if (content != updated) {
      validateIntegrity(updated, chapters)
      contentRepo.put(updated)
    }
  }
}

/**
 * Resolves a file or folder into the whole books it contains, independent of how
 * deeply they are nested:
 * - an audio file is a single-file book,
 * - a folder that directly holds audio is one whole book (nested sub-folders such
 *   as discs or bonus content are folded in as chapters by the [ChapterParser]),
 * - a folder whose audio lives only in sub-folders is either a single multi-disc
 *   book (when those sub-folders look like discs/parts) or a container of several
 *   distinct books (e.g. an author or series folder), which is descended into.
 */
internal fun CachedDocumentFile.bookRoots(): List<CachedDocumentFile> {
  if (isFile) {
    return if (isAudioFile()) listOf(this) else emptyList()
  }
  val children = children
  if (children.any { it.isAudioFile() }) {
    return listOf(this)
  }
  val audioSubFolders = children.filter { child ->
    child.isDirectory && child.walk().any { it.isAudioFile() }
  }
  return when {
    // No audio anywhere: keep the folder itself as a (currently empty) book so a
    // known book whose files are temporarily gone keeps its saved position.
    audioSubFolders.isEmpty() -> listOf(this)
    audioSubFolders.all { it.looksLikeBookPart() } -> listOf(this)
    else -> audioSubFolders.flatMap { it.bookRoots() }
  }
}

private fun CachedDocumentFile.looksLikeBookPart(): Boolean {
  val name = name?.trim()?.lowercase() ?: return false
  return name.toIntOrNull() != null || BOOK_PART_REGEX.matches(name)
}

private val BOOK_PART_REGEX =
  Regex("""^(cd|dis[ck]|part|pt|volume|vol|tape|chapter|ch)[\s._-]*\d+$""")
