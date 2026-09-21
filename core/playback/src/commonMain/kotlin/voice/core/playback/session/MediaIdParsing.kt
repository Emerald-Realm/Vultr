package voice.core.playback.session

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

fun String.toMediaIdOrNull(): MediaId? = try {
  Json.decodeFromString(MediaId.serializer(), this)
} catch (e: SerializationException) {
  null
}
