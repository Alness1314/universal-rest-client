package com.alness.universalrestclient.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Cancellable future that coordinates cancellation with a transport call. */
public final class RestCallFuture<T> extends CompletableFuture<HttpResponse<T>> {
    private final RestCall<T> call;

    private RestCallFuture(RestCall<T> call) {
        this.call = call;
    }

    public static <T> RestCallFuture<T> submit(RestCall<T> call, Executor executor) {
        Objects.requireNonNull(call, "call must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        RestCallFuture<T> future = new RestCallFuture<T>(call);
        executor.execute(() -> future.runCall());
        return future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean canceled = super.cancel(mayInterruptIfRunning);
        call.cancel();
        return canceled;
    }

    private void runCall() {
        if (isCancelled()) {
            return;
        }
        try {
            complete(call.execute());
        } catch (Throwable failure) {
            completeExceptionally(failure);
        }
    }
}
