package voice.core.data

import android.net.Uri
import androidx.core.net.toUri

public fun BookId(uri: Uri): BookId = BookId(uri.toString())

public fun BookId.toUri(): Uri = value.toUri()

public fun ChapterId(uri: Uri): ChapterId = ChapterId(uri.toString())

public fun ChapterId.toUri(): Uri = value.toUri()

public fun kotlinx.datetime.LocalTime.toJavaLocalTime(): java.time.LocalTime {
  return java.time.LocalTime.of(hour, minute, second, nanosecond)
}

public fun java.time.LocalTime.toKotlinxLocalTime(): kotlinx.datetime.LocalTime {
  return kotlinx.datetime.LocalTime(hour, minute, second, nano)
}
