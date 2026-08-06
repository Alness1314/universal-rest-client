package com.alness.universalrestclient.kotlin

import com.alness.universalrestclient.api.HttpRequest
import com.alness.universalrestclient.api.HttpResponse
import com.alness.universalrestclient.api.RestClient
import com.alness.universalrestclient.api.TypeRef
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Executes a request without blocking the calling coroutine. */
suspend fun <T> RestClient.await(
    request: HttpRequest,
    responseType: TypeRef<T>
): HttpResponse<T> = suspendCancellableCoroutine { continuation ->
    val future = executeAsync(request, responseType)
    continuation.invokeOnCancellation { future.cancel(true) }
    future.whenComplete { response, failure ->
        if (failure == null) {
            continuation.resume(response)
        } else if (continuation.isActive) {
            val cause = if (failure is CompletionException && failure.cause != null) {
                failure.cause!!
            } else {
                failure
            }
            continuation.resumeWithException(cause)
        }
    }
}
