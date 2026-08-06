package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;
import java.util.Arrays;

/** Base exception containing structured, sanitized request and response context. */
public class RestClientException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final FailureType failureType;
    private final HttpMethod method;
    private final URI uri;
    private final Integer statusCode;
    private final HttpHeaders headers;
    private final byte[] responseBody;
    private final boolean retryable;

    public RestClientException(String message) {
        this(message, null, FailureType.UNKNOWN, null, null, null,
                HttpHeaders.empty(), null, false);
    }

    public RestClientException(String message, Throwable cause) {
        this(message, cause, FailureType.UNKNOWN, null, null, null,
                HttpHeaders.empty(), null, false);
    }

    protected RestClientException(String message, Throwable cause, FailureType failureType,
                                  HttpMethod method, URI uri, Integer statusCode,
                                  HttpHeaders headers, byte[] responseBody, boolean retryable) {
        super(message, cause);
        this.failureType = failureType;
        this.method = method;
        this.uri = uri;
        this.statusCode = statusCode;
        this.headers = headers == null ? HttpHeaders.empty() : headers;
        this.responseBody = responseBody == null
                ? new byte[0] : Arrays.copyOf(responseBody, responseBody.length);
        this.retryable = retryable;
    }

    public FailureType failureType() {
        return failureType;
    }

    public HttpMethod method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public byte[] responseBody() {
        return Arrays.copyOf(responseBody, responseBody.length);
    }

    public boolean isRetryable() {
        return retryable;
    }
}
