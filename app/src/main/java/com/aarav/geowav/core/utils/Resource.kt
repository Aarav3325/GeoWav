package com.aarav.geowav.core.utils

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

enum class NetworkFailure {
    NoInternet,
    Timeout,
    ServerError,
    Unknown
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class NoInternet<T>(message: String = "No internet connection", data: T? = null) :
        Resource<T>(data, message)

    class Timeout<T>(message: String = "Taking longer than expected", data: T? = null) :
        Resource<T>(data, message)

    class ServerError<T>(message: String = "Server error", data: T? = null) :
        Resource<T>(data, message)

    class UnknownError<T>(message: String = "Something went wrong", data: T? = null) :
        Resource<T>(data, message)

    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

val Resource<*>.failure: NetworkFailure?
    get() = when (this) {
        is Resource.NoInternet -> NetworkFailure.NoInternet
        is Resource.Timeout -> NetworkFailure.Timeout
        is Resource.ServerError -> NetworkFailure.ServerError
        is Resource.UnknownError,
        is Resource.Error -> NetworkFailure.Unknown
        is Resource.Loading,
        is Resource.Success -> null
    }

const val DEFAULT_NETWORK_TIMEOUT_MS = 10_000L

suspend inline fun <T> withNetworkTimeout(
    timeoutMillis: Long = DEFAULT_NETWORK_TIMEOUT_MS,
    crossinline block: suspend () -> T
): Resource<T> {
    return try {
        Resource.Success(withTimeout(timeoutMillis) { block() })
    } catch (e: TimeoutCancellationException) {
        Resource.Timeout()
    } catch (e: SocketTimeoutException) {
        Resource.Timeout()
    } catch (e: IOException) {
        Resource.NoInternet()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.toNetworkResource()
    }
}

fun <T> Throwable.toNetworkResource(
    fallbackMessage: String = "Something went wrong"
): Resource<T> {
    return when {
        this is TimeoutCancellationException || this is SocketTimeoutException ->
            Resource.Timeout()

        this is IOException ->
            Resource.NoInternet()

        message.orEmpty().contains("network", ignoreCase = true) ||
            message.orEmpty().contains("internet", ignoreCase = true) ||
            message.orEmpty().contains("connection", ignoreCase = true) ->
            Resource.NoInternet()

        message.orEmpty().contains("permission_denied", ignoreCase = true) ||
            message.orEmpty().contains("unavailable", ignoreCase = true) ||
            message.orEmpty().contains("internal", ignoreCase = true) ->
            Resource.ServerError(message ?: "Server error")

        else -> Resource.UnknownError(message ?: fallbackMessage)
    }
}

fun NetworkFailure?.messageFor(subject: String, fallback: String? = null): String {
    return when (this) {
        NetworkFailure.NoInternet -> "No internet connection"
        NetworkFailure.Timeout -> "We're still loading $subject. Please try again."
        NetworkFailure.ServerError -> "We couldn't load $subject right now."
        NetworkFailure.Unknown, null -> fallback ?: "We couldn't load $subject right now."
    }
}
