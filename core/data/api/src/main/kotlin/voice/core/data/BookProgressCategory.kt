package voice.core.data

import java.util.concurrent.TimeUnit.SECONDS

public enum class BookProgressCategory {
  NOT_STARTED,
  CURRENT,
  FINISHED,
}

public val Book.progressCategory: BookProgressCategory
  get() {
    return if (position == 0L) {
      BookProgressCategory.NOT_STARTED
    } else if (position >= duration - SECONDS.toMillis(5)) {
      BookProgressCategory.FINISHED
    } else {
      BookProgressCategory.CURRENT
    }
  }
