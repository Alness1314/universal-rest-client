package com.alness.universalrestclient.api;

import java.io.IOException;
import java.io.OutputStream;

/** Repeatable or one-shot request body independent of an HTTP transport. */
public interface HttpBody {
    String contentType();

    long contentLength();

    boolean isRepeatable();

    void writeTo(OutputStream output) throws IOException;
}
