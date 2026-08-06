package com.alness.universalrestclient.exception;

/** Stable categories consumers can use without inspecting exception messages. */
public enum FailureType {
    CONNECTION,
    CONNECTION_TIMEOUT,
    READ_TIMEOUT,
    DNS,
    TLS,
    CANCELED,
    SERIALIZATION,
    INVALID_REQUEST,
    INVALID_URL,
    HTTP_STATUS,
    RESPONSE_TOO_LARGE,
    UNKNOWN
}
