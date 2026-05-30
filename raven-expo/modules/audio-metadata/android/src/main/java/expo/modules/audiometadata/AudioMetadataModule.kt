package expo.modules.audiometadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.abs

class AudioMetadataModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("AudioMetadata")

    AsyncFunction("getMetadata") { uriString: String ->
      val ctx = appContext.reactContext ?: return@AsyncFunction null
      readMetadata(ctx, uriString)
    }

    AsyncFunction("getChapters") { uriString: String ->
      val ctx = appContext.reactContext ?: return@AsyncFunction emptyList<Map<String, Any?>>()
      try {
        readChapters(ctx, Uri.parse(uriString))
      } catch (e: Exception) {
        emptyList<Map<String, Any?>>()
      }
    }
  }

  private fun readMetadata(ctx: Context, uriString: String): Map<String, Any?>? {
    val mmr = MediaMetadataRetriever()
    var pfd: ParcelFileDescriptor? = null
    try {
      when {
        uriString.startsWith("content://") -> {
          pfd = ctx.contentResolver.openFileDescriptor(Uri.parse(uriString), "r") ?: return null
          mmr.setDataSource(pfd.fileDescriptor)
        }
        uriString.startsWith("file://") -> mmr.setDataSource(Uri.parse(uriString).path)
        else -> mmr.setDataSource(uriString)
      }
      val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
      val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
        ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
      val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
      val durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()

      var artworkUri: String? = null
      val pic = mmr.embeddedPicture
      if (pic != null) {
        val out = File(ctx.cacheDir, "raven_cover_${abs(uriString.hashCode())}.jpg")
        out.writeBytes(pic)
        artworkUri = Uri.fromFile(out).toString()
      }

      return mapOf(
        "title" to title,
        "artist" to artist,
        "album" to album,
        "durationMs" to durationMs,
        "artworkUri" to artworkUri,
      )
    } catch (e: Exception) {
      return null
    } finally {
      try { mmr.release() } catch (_: Exception) {}
      try { pfd?.close() } catch (_: Exception) {}
    }
  }

  // ---- MP4 chapter reading (Nero `chpl` box inside moov/udta) ----

  private fun readChapters(ctx: Context, uri: Uri): List<Map<String, Any?>> {
    val pfd = ctx.contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()
    pfd.use {
      FileInputStream(it.fileDescriptor).channel.use { ch ->
        val moov = findTopLevelBox(ch, "moov") ?: return emptyList()
        if (moov.second > 64L * 1024 * 1024) return emptyList()
        val bytes = ByteArray(moov.second.toInt())
        ch.position(moov.first)
        if (!readFully(ch, ByteBuffer.wrap(bytes))) return emptyList()
        val childStart = if (readU32(bytes, 0) == 1L) 16 else 8
        val udta = findBox(bytes, childStart, bytes.size, "udta") ?: return emptyList()
        val chpl = findBox(bytes, udta.first, udta.second, "chpl") ?: return emptyList()
        return parseChpl(bytes, chpl.first, chpl.second)
      }
    }
  }

  // Returns (startOffset, totalSize) of the first top-level box of [type].
  private fun findTopLevelBox(ch: FileChannel, type: String): Pair<Long, Long>? {
    val size = ch.size()
    var pos = 0L
    val head = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
    while (pos + 8 <= size) {
      ch.position(pos)
      head.clear()
      if (!readFully(ch, head)) break
      head.flip()
      var boxSize = head.int.toLong() and 0xFFFFFFFFL
      val t = ByteArray(4)
      head.get(t)
      var headerSize = 8L
      if (boxSize == 1L) {
        val ext = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN)
        ch.position(pos + 8)
        if (!readFully(ch, ext)) break
        ext.flip()
        boxSize = ext.long
        headerSize = 16L
      } else if (boxSize == 0L) {
        boxSize = size - pos
      }
      if (boxSize < headerSize) break
      if (String(t, Charsets.US_ASCII) == type) return Pair(pos, boxSize)
      pos += boxSize
    }
    return null
  }

  // Finds a child box by type within [start, end); returns its content range (after header).
  private fun findBox(b: ByteArray, start: Int, end: Int, type: String): Pair<Int, Int>? {
    var pos = start
    while (pos + 8 <= end) {
      var boxSize = readU32(b, pos)
      var headerSize = 8
      if (boxSize == 1L) {
        boxSize = readU64(b, pos + 8)
        headerSize = 16
      } else if (boxSize == 0L) {
        boxSize = (end - pos).toLong()
      }
      if (boxSize < headerSize || pos + boxSize > end) break
      val t = String(b, pos + 4, 4, Charsets.US_ASCII)
      if (t == type) return Pair(pos + headerSize, (pos + boxSize).toInt())
      pos += boxSize.toInt()
    }
    return null
  }

  // Nero chapter list. Mirrors FFmpeg's mov_read_chpl. Times are in 100ns units.
  private fun parseChpl(b: ByteArray, start: Int, end: Int): List<Map<String, Any?>> {
    var p = start
    if (p + 5 > end) return emptyList()
    val version = b[p].toInt() and 0xFF
    p += 4 // version (1) + flags (3)
    if (version != 0) p += 4 // reserved when version != 0
    if (p >= end) return emptyList()
    val count = b[p].toInt() and 0xFF
    p += 1
    val out = ArrayList<Map<String, Any?>>(count)
    for (i in 0 until count) {
      val startUnits: Long
      if (version != 0) {
        if (p + 8 > end) break
        startUnits = readU64(b, p); p += 8
      } else {
        if (p + 4 > end) break
        startUnits = readU32(b, p); p += 4
      }
      if (p >= end) break
      val titleLen = b[p].toInt() and 0xFF; p += 1
      if (p + titleLen > end) break
      val title = String(b, p, titleLen, Charsets.UTF_8); p += titleLen
      out.add(mapOf("title" to title, "startMs" to startUnits / 10000L))
    }
    return out
  }

  private fun readU32(b: ByteArray, o: Int): Long =
    ((b[o].toLong() and 0xFF) shl 24) or
      ((b[o + 1].toLong() and 0xFF) shl 16) or
      ((b[o + 2].toLong() and 0xFF) shl 8) or
      (b[o + 3].toLong() and 0xFF)

  private fun readU64(b: ByteArray, o: Int): Long {
    var v = 0L
    for (i in 0 until 8) v = (v shl 8) or (b[o + i].toLong() and 0xFF)
    return v
  }

  private fun readFully(ch: FileChannel, buf: ByteBuffer): Boolean {
    while (buf.hasRemaining()) {
      if (ch.read(buf) < 0) return false
    }
    return true
  }
}
