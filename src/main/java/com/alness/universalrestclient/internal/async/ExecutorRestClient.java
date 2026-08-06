package com.alness.universalrestclient.internal.async;

import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.RestCallFuture;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TypeRef;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Applies a caller-owned executor to asynchronous operations. */
public final class ExecutorRestClient implements RestClient {
    private final RestClient delegate;
    private final Executor executor;

    public ExecutorRestClient(RestClient delegate, Executor executor) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    @Override
    public <T> RestCall<T> newCall(HttpRequest request, TypeRef<T> responseType) {
        return delegate.newCall(request, responseType);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> executeAsync(
            HttpRequest request, TypeRef<T> responseType) {
        return RestCallFuture.submit(newCall(request, responseType), executor);
    }
}
