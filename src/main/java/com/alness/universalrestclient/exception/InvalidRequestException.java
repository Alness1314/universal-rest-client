package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;

/** Indicates that a request cannot be executed because its input is invalid. */
public final class InvalidRequestException extends RestClientException {
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        this(message, null, FailureType.INVALID_REQUEST, null, null);
    }

    public InvalidRequestException(String message, Throwable cause) {
        this(message, cause, FailureType.INVALID_REQUEST, null, null);
    }

    public InvalidRequestException(String message, Throwable cause, FailureType failureType,
                                   HttpMethod method, URI uri) {
        super(message, cause, failureType, method, uri, null, null, null, false);
    }
}
