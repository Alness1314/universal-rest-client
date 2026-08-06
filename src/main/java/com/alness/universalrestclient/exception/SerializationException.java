package com.alness.universalrestclient.exception;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;

import java.net.URI;

/** Indicates that a request or response body could not be converted. */
public final class SerializationException extends RestClientException {
    private static final long serialVersionUID = 1L;

    public SerializationException(String message, Throwable cause) {
        this(message, cause, null, null, null, null, null);
    }

    public SerializationException(String message, Throwable cause, HttpMethod method, URI uri,
                                  Integer statusCode, HttpHeaders headers, byte[] responseBody) {
        super(message, cause, FailureType.SERIALIZATION, method, uri, statusCode,
                headers, responseBody, false);
    }
}
