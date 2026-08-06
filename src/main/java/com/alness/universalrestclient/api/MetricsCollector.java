package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.FailureType;

import java.net.URI;

/** Minimal metrics port that can be implemented with Micrometer or another backend. */
public interface MetricsCollector {
    void record(HttpMethod method, URI uri, Integer statusCode,
                FailureType failureType, long elapsedMillis);
}
