package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.ExchangeObserver;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.MetricsCollector;
import com.alness.universalrestclient.exception.RestClientException;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;

import java.util.Objects;

/** Bridges exchange events to a framework-independent metrics collector. */
public final class MetricsObserver implements ExchangeObserver {
    private final MetricsCollector collector;

    public MetricsObserver(MetricsCollector collector) {
        this.collector = Objects.requireNonNull(collector, "collector must not be null");
    }

    @Override
    public void onRequest(HttpRequest request) {
    }

    @Override
    public void onResponse(HttpRequest request, HttpResponse<?> response, long elapsedMillis) {
        collector.record(request.method(), SensitiveDataSanitizer.uri(request.uri()),
                response.statusCode(), null, elapsedMillis);
    }

    @Override
    public void onFailure(HttpRequest request, RestClientException failure, long elapsedMillis) {
        collector.record(request.method(), SensitiveDataSanitizer.uri(request.uri()),
                failure.statusCode(), failure.failureType(), elapsedMillis);
    }
}
