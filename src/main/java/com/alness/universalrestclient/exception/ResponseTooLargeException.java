package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;

/** Indicates that a response exceeded the configured in-memory limit. */
public final class ResponseTooLargeException extends RestClientException {
    private static final long serialVersionUID = 1L;
    private final long maximumBytes;

    public ResponseTooLargeException(HttpMethod method, URI uri, long maximumBytes, Throwable cause) {
        super("HTTP response exceeded the configured size limit", cause,
                FailureType.RESPONSE_TOO_LARGE, method, uri, null, null, null, false);
        this.maximumBytes = maximumBytes;
    }

    public long maximumBytes() {
        return maximumBytes;
    }
}
