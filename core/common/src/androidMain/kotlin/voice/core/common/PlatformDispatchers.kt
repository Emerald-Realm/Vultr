package voice.core.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual val platformIoDispatcher: CoroutineContext = Dispatchers.IO
