package voice.core.playback.session

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.junit.Test

class CustomCommandTest {

  @Test
  fun `new commands round-trip through json`() {
    listOf(CustomCommand.AddBookmark, CustomCommand.CycleSpeed).forEach { command ->
      val json = Json.encodeToString(CustomCommand.serializer(), command)
      Json.decodeFromString(CustomCommand.serializer(), json) shouldBe command
    }
  }
}
