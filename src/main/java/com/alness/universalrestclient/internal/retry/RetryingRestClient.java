package com.alness.universalrestclient.internal.retry;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.RetryMode;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.RetryPolicy;
import com.alness.universalrestclient.exception.FailureType;
import com.alness.universalrestclient.exception.HttpStatusException;
import com.alness.universalrestclient.exception.RestClientException;
import com.alness.universalrestclient.exception.TransportException;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;

/** Retry decorator that never repeats unsafe requests unless explicitly enabled. */
public final class RetryingRestClient implements RestClient {
    private final RestClient delegate;
    private final RetryPolicy policy;
    private final Sleeper sleeper;
    private final LongSupplier clock;

    public RetryingRestClient(RestClient delegate, RetryPolicy policy) {
        this(delegate, policy, Thread::sleep, System::currentTimeMillis);
    }

    RetryingRestClient(RestClient delegate, RetryPolicy policy, Sleeper sleeper,
                       LongSupplier clock) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public <T> RestCall<T> newCall(HttpRequest request, TypeRef<T> responseType) {
        return new RetryingCall<T>(delegate, policy, sleeper, clock, request, responseType);
    }

    private static final class RetryingCall<T> implements RestCall<T> {
        private final RestClient delegate;
        private final RetryPolicy policy;
        private final Sleeper sleeper;
        private final LongSupplier clock;
        private final HttpRequest request;
        private final TypeRef<T> responseType;
        private volatile RestCall<T> activeCall;
        private volatile Thread executingThread;
        private volatile boolean canceled;
        private volatile boolean executed;

        private RetryingCall(RestClient delegate, RetryPolicy policy, Sleeper sleeper,
                             LongSupplier clock, HttpRequest request, TypeRef<T> responseType) {
            this.delegate = delegate;
            this.policy = policy;
            this.sleeper = sleeper;
            this.clock = clock;
            this.request = request;
            this.responseType = responseType;
        }

        @Override
        public synchronized HttpResponse<T> execute() {
            if (executed) {
                throw new IllegalStateException("A call can only be executed once");
            }
            executed = true;
            executingThread = Thread.currentThread();
            try {
                return executeAttempts();
            } finally {
                executingThread = null;
            }
        }

        private HttpResponse<T> executeAttempts() {
            int attempt = 1;
            while (true) {
                ensureNotCanceled();
                activeCall = delegate.newCall(request, responseType);
                try {
                    HttpResponse<T> response = activeCall.execute();
                    if (!shouldRetryStatus(response.statusCode(), attempt)) {
                        return response;
                    }
                    waitBeforeRetry(attempt, response.headers());
                } catch (HttpStatusException exception) {
                    if (!canRetry(attempt) || !exception.isRetryable()) {
                        throw exception;
                    }
                    waitBeforeRetry(attempt, exception.headers());
                } catch (TransportException exception) {
                    if (!canRetry(attempt) || !exception.isRetryable()) {
                        throw exception;
                    }
                    waitBeforeRetry(attempt, HttpHeaders.empty());
                }
                attempt++;
            }
        }

        private boolean shouldRetryStatus(int statusCode, int attempt) {
            return canRetry(attempt) && retryableStatus(statusCode);
        }

        private boolean canRetry(int attempt) {
            if (attempt >= policy.maxAttempts() || request.retryMode() == RetryMode.DISABLED) {
                return false;
            }
            if (request.body() != null && !request.body().isRepeatable()) {
                return false;
            }
            if (request.retryMode() == RetryMode.ENABLED) {
                return true;
            }
            HttpMethod method = request.method();
            return method == HttpMethod.GET || method == HttpMethod.HEAD
                    || method == HttpMethod.OPTIONS || method == HttpMethod.PUT
                    || method == HttpMethod.DELETE;
        }

        private void waitBeforeRetry(int attempt, HttpHeaders headers) {
            ensureNotCanceled();
            long delay = retryAfter(headers);
            if (delay < 0) {
                delay = exponentialDelay(attempt);
            }
            try {
                sleeper.sleep(delay);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                ensureNotCanceled();
                throw new TransportException("Retry wait was interrupted", exception,
                        FailureType.CANCELED, request.method(), sanitizedUri(), false);
            }
            ensureNotCanceled();
        }

        private long exponentialDelay(int attempt) {
            long multiplier = 1L << Math.min(attempt - 1, 30);
            long base = Math.min(policy.maxDelayMillis(), safeMultiply(
                    policy.initialDelayMillis(), multiplier));
            if (base == 0 || policy.jitterFactor() == 0) {
                return base;
            }
            double range = base * policy.jitterFactor();
            double random = ThreadLocalRandom.current().nextDouble(-range, range);
            return Math.max(0, Math.round(base + random));
        }

        private long retryAfter(HttpHeaders headers) {
            java.util.Optional<String> value = headers.firstValue("Retry-After");
            if (!value.isPresent()) {
                return -1;
            }
            try {
                return Math.max(0, Long.parseLong(value.get().trim()) * 1_000L);
            } catch (NumberFormatException ignored) {
                try {
                    long timestamp = ZonedDateTime.parse(value.get(),
                            DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
                    return Math.max(0, timestamp - clock.getAsLong());
                } catch (DateTimeParseException invalidDate) {
                    return -1;
                }
            }
        }

        @Override
        public void cancel() {
            canceled = true;
            RestCall<T> call = activeCall;
            if (call != null) {
                call.cancel();
            }
            Thread thread = executingThread;
            if (thread != null) {
                thread.interrupt();
            }
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public boolean isExecuted() {
            return executed;
        }

        private void ensureNotCanceled() {
            if (canceled) {
                throw new TransportException("HTTP call was canceled", null,
                        FailureType.CANCELED, request.method(), sanitizedUri(), false);
            }
        }

        private URI sanitizedUri() {
            return SensitiveDataSanitizer.uri(request.uri());
        }

        private static boolean retryableStatus(int statusCode) {
            return statusCode == 408 || statusCode == 425 || statusCode == 429
                    || statusCode >= 500;
        }

        private static long safeMultiply(long left, long right) {
            if (left != 0 && right > Long.MAX_VALUE / left) {
                return Long.MAX_VALUE;
            }
            return left * right;
        }
    }
}
