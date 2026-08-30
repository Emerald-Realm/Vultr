package voice.core.data

import kotlinx.serialization.Serializable

@Serializable
public data class ReaderProfile(
  val name: String = DEFAULT_NAME,
  val imageUri: String? = null,
) {
  public companion object {
    public const val DEFAULT_NAME: String = "Reader"
  }
}
