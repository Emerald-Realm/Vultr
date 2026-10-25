package voice.core.data

import kotlinx.serialization.Serializable

@Serializable
public data class RatingReminderState(
  val lastPresentedMonth: String? = null,
  val selfDeclaredRated: Boolean = false,
)
