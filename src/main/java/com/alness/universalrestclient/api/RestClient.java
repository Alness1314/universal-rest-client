package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.RestClientException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/** Transport-neutral synchronous REST client contract. */
public interface RestClient {
    <T> RestCall<T> newCall(HttpRequest request, TypeRef<T> responseType);

    default <T> HttpResponse<T> execute(HttpRequest request, TypeRef<T> responseType)
            throws RestClientException {
        return newCall(request, responseType).execute();
    }

    default <T> CompletableFuture<HttpResponse<T>> executeAsync(
            HttpRequest request, TypeRef<T> responseType) {
        return RestCallFuture.submit(newCall(request, responseType), ForkJoinPool.commonPool());
    }
}
