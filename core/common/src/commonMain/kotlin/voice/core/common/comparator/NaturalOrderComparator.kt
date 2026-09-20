package voice.core.common.comparator

object NaturalOrderComparator {

  val stringComparator: Comparator<String> = IntelliJStringComparator

  val uriStringComparator: Comparator<String> = object : Comparator<String> {
    override fun compare(
      lhs: String,
      rhs: String,
    ): Int {
      val lhsSegments = lhs.uriPathSegments().flatMap { it.split("/") }
      val rhsSegments = rhs.uriPathSegments().flatMap { it.split("/") }

      val leftSize = lhsSegments.size
      val rightSize = rhsSegments.size

      // compare parents only and return if one differs
      var i = 0
      val toLeft = leftSize - 1
      val toRight = rightSize - 1
      while (i < toLeft && i < toRight) {
        val pl = lhsSegments[i]
        val pr = rhsSegments[i]
        if (pl != pr) {
          return stringComparator.compare(pl, pr)
        }
        i++
      }

      // if sizes are the same
      return if (leftSize == rightSize) {
        stringComparator.compare(lhsSegments.lastOrNull() ?: "", rhsSegments.lastOrNull() ?: "")
      } else {
        rightSize - leftSize
      }
    }
  }
}

fun Set<String>.sortedNaturally(): List<String> = sortedWith(NaturalOrderComparator.stringComparator)

private fun String.uriPathSegments(): List<String> {
  val schemeSeparator = indexOf("://")
  val pathStart = if (schemeSeparator >= 0) {
    indexOf('/', schemeSeparator + 3)
  } else {
    indexOf('/')
  }
  if (pathStart < 0) {
    return emptyList()
  }
  return substring(pathStart)
    .trim('/')
    .split('/')
    .map { it.percentDecode() }
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
