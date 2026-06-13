package voice.core.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.FileTypes
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.extractor.metadata.flac.PictureFrame
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.inspector.MetadataRetriever
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.guava.await
import voice.core.logging.api.Logger
import voice.core.scanner.matroska.MatroskaCoverExtractor
import java.io.File

@Inject
internal class CoverExtractor(
  private val context: Context,
  private val matroskaCoverExtractor: MatroskaCoverExtractor,
) {

  suspend fun extractCover(
    input: Uri,
    outputFile: File,
  ): Boolean {
    val fileType = FileTypes.inferFileTypeFromUri(input)
    val extension = (input.path ?: "").substringAfterLast(delimiter = ".", missingDelimiterValue = "").lowercase()
    if (fileType == FileTypes.MATROSKA || extension == "mka" || extension == "mkv") {
      return matroskaCoverExtractor.extract(input, outputFile)
    }

    val trackGroups = retrieveMetadata(input)
      ?: return false

    repeat(trackGroups.length) { trackGroupIndex ->
      val trackGroup = trackGroups[trackGroupIndex]
      repeat(trackGroup.length) { formatIndex ->
        val format = trackGroup.getFormat(formatIndex)
        val metadata = format.metadata
        if (metadata != null) {
          repeat(metadata.length()) { metadataIndex ->
            when (val entry = metadata.get(metadataIndex)) {
              is ApicFrame -> {
                Logger.d("Found embedded cover in ${trackGroup.type}")
                if (writeCover(entry.pictureData, outputFile)) return true
              }
              is PictureFrame -> {
                Logger.d("Found embedded cover in ${trackGroup.type}")
                if (writeCover(entry.pictureData, outputFile)) return true
              }
              else -> {
                Logger.v("Unknown metadata entry: $entry")
              }
            }
          }
        }
      }
    }
    return false
  }

  private suspend fun retrieveMetadata(uri: Uri): TrackGroupArray? {
    return try {
      MetadataRetriever.Builder(context, MediaItem.fromUri(uri))
        .build()
        .retrieveTrackGroups()
        .await()
    } catch (e: Exception) {
      if (e is CancellationException) currentCoroutineContext().ensureActive()
      Logger.w(e, "Error retrieving metadata")
      null
    }
  }

  // Embedded audiobook covers are often far larger than they are ever displayed.
  // Oversized art is downscaled to keep storage small and avoid loading huge
  // bitmaps; anything already reasonably sized is stored untouched. If decoding
  // fails for any reason we fall back to the raw bytes so a cover is never lost.
  private fun writeCover(
    data: ByteArray,
    outputFile: File,
  ): Boolean {
    return try {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
      val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
      if (bounds.outWidth <= 0 || largestSide <= MAX_COVER_DIMENSION) {
        return writeRaw(data, outputFile)
      }
      val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
      }
      val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
        ?: return writeRaw(data, outputFile)
      try {
        outputFile.outputStream().use { output ->
          bitmap.compress(Bitmap.CompressFormat.JPEG, COVER_QUALITY, output)
        }
      } finally {
        bitmap.recycle()
      }
      true
    } catch (e: Exception) {
      Logger.w(e, "Error scaling cover, storing raw bytes")
      writeRaw(data, outputFile)
    }
  }

  private fun writeRaw(
    data: ByteArray,
    outputFile: File,
  ): Boolean {
    return try {
      outputFile.outputStream().use { output -> output.write(data) }
      true
    } catch (e: Exception) {
      Logger.w(e, "Error writing cover")
      false
    }
  }

  private fun sampleSizeFor(
    width: Int,
    height: Int,
  ): Int {
    var sampleSize = 1
    while (maxOf(width, height) / (sampleSize * 2) >= MAX_COVER_DIMENSION) {
      sampleSize *= 2
    }
    return sampleSize
  }
}

private const val MAX_COVER_DIMENSION = 1500
private const val COVER_QUALITY = 90
