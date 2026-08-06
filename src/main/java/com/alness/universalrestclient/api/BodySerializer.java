package com.alness.universalrestclient.api;

import com.alness.universalrestclient.exception.SerializationException;

/** Converts typed values into transport-neutral HTTP bodies. */
public interface BodySerializer {
    HttpBody serialize(Object value, TypeRef<?> type) throws SerializationException;
}
