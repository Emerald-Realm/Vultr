package voice.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

data class DispatcherProvider(
  val main: CoroutineContext = Dispatchers.Main,
  val io: CoroutineContext = platformIoDispatcher,
  val mainImmediate: CoroutineContext = Dispatchers.Main.immediate,
)

internal expect val platformIoDispatcher: CoroutineContext

@Suppress("FunctionName")
fun MainScope(dispatcherProvider: DispatcherProvider): CoroutineScope {
  return CoroutineScope(SupervisorJob() + dispatcherProvider.mainImmediate)
}
