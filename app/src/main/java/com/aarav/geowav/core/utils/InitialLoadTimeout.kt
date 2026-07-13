package com.aarav.geowav.core.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

sealed interface InitialLoadEvent<out T> {
    object TimedOut : InitialLoadEvent<Nothing>
    data class Value<T>(val value: T) : InitialLoadEvent<T>
}

fun <T> Flow<T>.withInitialLoadTimeout(
    timeoutMillis: Long = DEFAULT_NETWORK_TIMEOUT_MS
): Flow<InitialLoadEvent<T>> = channelFlow {
    var hasFirstValue = false
    val timeoutJob = launch {
        delay(timeoutMillis)
        if (!hasFirstValue) {
            send(InitialLoadEvent.TimedOut)
        }
    }

    collect { value ->
        if (!hasFirstValue) {
            hasFirstValue = true
            timeoutJob.cancel()
        }
        send(InitialLoadEvent.Value(value))
    }
}
