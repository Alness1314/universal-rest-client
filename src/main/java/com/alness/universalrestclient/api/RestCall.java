package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.RestClientException;

/** A single-use HTTP operation that can be canceled from another thread. */
public interface RestCall<T> {
    HttpResponse<T> execute() throws RestClientException;

    void cancel();

    boolean isCanceled();

    boolean isExecuted();
}
