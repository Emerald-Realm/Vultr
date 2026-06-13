package voice.core.scanner

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import voice.core.data.Book
import voice.core.data.toUri
import voice.core.logging.api.Logger
import java.io.IOException

@Inject
internal class CoverScanner(
  private val context: Context,
  private val coverSaver: CoverSaver,
  private val coverExtractor: CoverExtractor,
) {

  suspend fun scan(books: List<Book>) {
    books.forEach { findCoverForBook(it) }
  }

  private suspend fun findCoverForBook(book: Book) {
    val coverFile = book.content.cover
    if (coverFile != null && coverFile.exists()) {
      return
    }

    val foundOnDisc = findAndSaveCoverFromDisc(book)
    if (foundOnDisc) {
      return
    }

    scanForEmbeddedCover(book)
  }

  private suspend fun findAndSaveCoverFromDisc(book: Book): Boolean = withContext(Dispatchers.IO) {
    val documentFile = try {
      DocumentFile.fromTreeUri(context, book.id.toUri())
    } catch (_: IllegalArgumentException) {
      null
    } ?: return@withContext false

    if (!documentFile.isDirectory) {
      return@withContext false
    }

    // Prefer images that are conventionally the book art (cover/folder/front/…)
    // over an arbitrary image that happens to come first in the listing.
    val images = documentFile.listFiles()
      .filter { it.isFile && it.canRead() && it.type?.startsWith("image/") == true }
      .sortedBy { coverNamePriority(it.name) }

    for (child in images) {
      val coverFile = coverSaver.newBookCoverFile()
      val worked = try {
        context.contentResolver.openInputStream(child.uri)?.use { input ->
          coverFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        true
      } catch (e: IOException) {
        Logger.w(e, "Error while copying the cover from ${child.uri}")
        false
      } catch (e: IllegalStateException) {
        // On some Samsung Devices, openInputStream throws this exception, though it should not.
        Logger.w(e, "Error while copying the cover from ${child.uri}")
        false
      }
      if (worked) {
        coverSaver.setBookCover(coverFile, book.id)
        return@withContext true
      }
    }

    false
  }

  private fun coverNamePriority(name: String?): Int {
    val lower = name?.lowercase() ?: return LAST_PRIORITY
    return when {
      lower.startsWith("cover") -> 0
      lower.startsWith("folder") -> 1
      lower.startsWith("front") -> 2
      lower.startsWith("album") || lower.contains("albumart") -> 3
      lower.contains("artwork") || lower.contains("art") -> 4
      else -> LAST_PRIORITY
    }
  }

  private suspend fun scanForEmbeddedCover(book: Book) {
    val coverFile = coverSaver.newBookCoverFile()
    book.chapters
      .take(5).forEach { chapter ->
        val success = coverExtractor.extractCover(
          input = chapter.id.toUri(),
          outputFile = coverFile,
        )
        if (success && coverFile.exists() && coverFile.length() > 0) {
          coverSaver.setBookCover(coverFile, bookId = book.id)
          return
        }
      }
  }
}

private const val LAST_PRIORITY = Int.MAX_VALUE
