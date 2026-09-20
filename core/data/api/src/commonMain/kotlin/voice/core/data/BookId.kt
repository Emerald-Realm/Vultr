package voice.core.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = BookIdSerializer::class)
public data class BookId(val value: String)

public object BookIdSerializer : KSerializer<BookId> {

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("bookId", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BookId = BookId(decoder.decodeString())

  override fun serialize(
    encoder: Encoder,
    value: BookId,
  ) {
    encoder.encodeString(value.value)
  }
}
