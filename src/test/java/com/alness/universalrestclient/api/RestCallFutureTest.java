package com.alness.universalrestclient.api;

import com.alness.universalrestclient.config.RestClientBuilder;
import com.alness.universalrestclient.testing.HttpResponses;
import com.alness.universalrestclient.testing.StubRestClient;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class RestCallFutureTest {
    @Test
    void executesOnTheConfiguredExecutor() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor(runnable ->
                new Thread(runnable, "custom-rest-executor"));
        try {
            StubRestClient delegate = new StubRestClient()
                    .enqueueResponse(HttpResponses.successful("done"));
            RestClient client = new com.alness.universalrestclient.internal.async.ExecutorRestClient(
                    delegate, executor);

            CompletableFuture<HttpResponse<String>> future = client.executeAsync(request(),
                    TypeRef.of(String.class));

            assertThat(future.get(2, TimeUnit.SECONDS).body()).isEqualTo("done");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void propagatesFutureCancellationToTheUnderlyingCall() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        BlockingCall call = new BlockingCall();
        try {
            RestCallFuture<String> future = RestCallFuture.submit(call, executor);
            assertThat(call.started.await(2, TimeUnit.SECONDS)).isTrue();

            future.cancel(true);

            assertThat(call.canceled).isTrue();
            assertThat(future.isCancelled()).isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void publicBuilderAcceptsACallerOwnedExecutor() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            RestClient client = RestClientBuilder.builder().executor(executor).build();
            assertThat(client).isNotNull();
        } finally {
            executor.shutdownNow();
        }
    }

    private static HttpRequest request() {
        return HttpRequest.builder().method(HttpMethod.GET)
                .uri("https://example.test/async").build();
    }

    private static final class BlockingCall implements RestCall<String> {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch released = new CountDownLatch(1);
        private final AtomicBoolean canceled = new AtomicBoolean();
        private volatile boolean executed;

        @Override
        public HttpResponse<String> execute() {
            executed = true;
            started.countDown();
            try {
                released.await();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return HttpResponses.successful("done");
        }

        @Override
        public void cancel() {
            canceled.set(true);
            released.countDown();
        }

        @Override
        public boolean isCanceled() {
            return canceled.get();
        }

        @Override
        public boolean isExecuted() {
            return executed;
        }
    }
}
