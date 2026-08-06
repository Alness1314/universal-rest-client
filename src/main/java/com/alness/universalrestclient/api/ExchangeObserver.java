package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.RestClientException;

/** Receives lifecycle events without changing requests or responses. */
public interface ExchangeObserver {
    void onRequest(HttpRequest request);

    void onResponse(HttpRequest request, HttpResponse<?> response, long elapsedMillis);

    void onFailure(HttpRequest request, RestClientException failure, long elapsedMillis);
}
