package voice.core.documentfile

interface CachedDocumentFile {
  val children: List<CachedDocumentFile>
  val name: String?
  val isDirectory: Boolean
  val isFile: Boolean
  val length: Long
  val lastModified: Long
  val uri: String
}

fun CachedDocumentFile.nameWithoutExtension(): String {
  val name = name
  return if (name == null) {
    uri.substringAfterLast('/')
      .percentDecode()
      .dropWhile { it != ':' }
      .removePrefix(":")
      .takeUnless { it.isBlank() }
      ?: uri
  } else {
    if (isFile) {
      name.substringBeforeLast(".")
        .takeUnless { it.isEmpty() }
        ?: name
    } else {
      name
    }
  }
}

private fun String.percentDecode(): String {
  if ('%' !in this) return this
  val bytes = ArrayList<Byte>(length)
  var i = 0
  while (i < length) {
    val c = this[i]
    if (c == '%' && i + 2 < length) {
      val value = substring(i + 1, i + 3).toIntOrNull(radix = 16)
      if (value != null) {
        bytes.add(value.toByte())
        i += 3
        continue
      }
    }
    c.toString().encodeToByteArray().forEach { bytes.add(it) }
    i++
  }
  return bytes.toByteArray().decodeToString()
}
