package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.ExchangeObserver;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.exception.RestClientException;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Metadata-only logger that redacts credentials and never logs bodies. */
public final class SafeLoggingObserver implements ExchangeObserver {
    private final Logger logger;
    private final Level level;

    public SafeLoggingObserver(Logger logger, Level level) {
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.level = Objects.requireNonNull(level, "level must not be null");
    }

    @Override
    public void onRequest(HttpRequest request) {
        logger.log(level, "HTTP request {0} {1} headers={2}", new Object[] {
                request.method(), SensitiveDataSanitizer.uri(request.uri()),
                SensitiveDataSanitizer.headers(request.headers()).asMap()
        });
    }

    @Override
    public void onResponse(HttpRequest request, HttpResponse<?> response, long elapsedMillis) {
        logger.log(level, "HTTP response {0} {1} status={2} elapsedMs={3}", new Object[] {
                request.method(), SensitiveDataSanitizer.uri(request.uri()),
                response.statusCode(), elapsedMillis
        });
    }

    @Override
    public void onFailure(HttpRequest request, RestClientException failure, long elapsedMillis) {
        logger.log(level, "HTTP failure {0} {1} type={2} elapsedMs={3}", new Object[] {
                request.method(), SensitiveDataSanitizer.uri(request.uri()),
                failure.failureType(), elapsedMillis
        });
    }
}
