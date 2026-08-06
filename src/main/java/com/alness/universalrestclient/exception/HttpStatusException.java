package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;

/** Represents an HTTP response rejected by the configured status policy. */
public final class HttpStatusException extends RestClientException {
    private static final long serialVersionUID = 1L;

    public HttpStatusException(int statusCode, HttpHeaders headers, byte[] responseBody) {
        this(null, null, statusCode, headers, responseBody, retryableStatus(statusCode));
    }

    public HttpStatusException(HttpMethod method, URI uri, int statusCode, HttpHeaders headers,
                               byte[] responseBody, boolean retryable) {
        super("HTTP request failed with status " + statusCode, null, FailureType.HTTP_STATUS,
                method, uri, statusCode, headers, responseBody, retryable);
    }

    private static boolean retryableStatus(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }
}
