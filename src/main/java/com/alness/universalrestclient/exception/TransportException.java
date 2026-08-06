package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;

/** Indicates that the underlying HTTP transport could not complete a request. */
public class TransportException extends RestClientException {
    private static final long serialVersionUID = 1L;

    public TransportException(String message, Throwable cause, boolean retryable) {
        this(message, cause, FailureType.UNKNOWN, null, null, retryable);
    }

    public TransportException(String message, Throwable cause, FailureType failureType,
                              HttpMethod method, URI uri, boolean retryable) {
        super(message, cause, failureType, method, uri, null, null, null, retryable);
    }
}
