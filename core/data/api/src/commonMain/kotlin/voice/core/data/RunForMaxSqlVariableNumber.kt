package voice.core.data

@PublishedApi
internal const val SQLITE_MAX_VARIABLE_NUMBER: Int = 990

// we can only query SQLITE_MAX_VARIABLE_NUMBER at once (999 bugs on some devices so we use a number a little smaller.)
// if it's larger than the limit, we query in chunks.
public inline fun <T, R> List<T>.runForMaxSqlVariableNumber(
  limit: Int = SQLITE_MAX_VARIABLE_NUMBER,
  action: (List<T>) -> List<R>,
): List<R> {
  return when {
    isEmpty() -> emptyList()
    size <= limit -> action(this)
    else -> chunked(limit).flatMap(action)
  }
}
