package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.SerializationException;

/** Converts raw response bytes into a requested Java type. */
public interface BodyDeserializer {
    <T> T deserialize(byte[] value, TypeRef<T> type, String contentType)
            throws SerializationException;
}
