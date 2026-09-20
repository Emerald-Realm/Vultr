package voice.core.data

import kotlinx.serialization.Serializable
import voice.core.common.comparator.NaturalOrderComparator

@Serializable(with = ChapterIdSerializer::class)
public data class ChapterId(val value: String) : Comparable<ChapterId> {

  override fun compareTo(other: ChapterId): Int {
    return NaturalOrderComparator.uriStringComparator.compare(value, other.value)
  }
}
