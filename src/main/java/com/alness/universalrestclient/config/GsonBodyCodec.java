package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.BodyCodec;
import com.alness.universalrestclient.api.HttpBodies;
import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.SerializationException;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Gson adapter. Gson remains an optional dependency of the library. */
public final class GsonBodyCodec implements BodyCodec {
    private final Gson gson;

    public GsonBodyCodec(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    public static GsonBodyCodec withDefaults() {
        return new GsonBodyCodec(new Gson());
    }

    public Gson gson() {
        return gson;
    }

    @Override
    public HttpBody serialize(Object value, TypeRef<?> type) {
        try {
            String json = gson.toJson(value, type.type());
            return HttpBodies.bytes(json.getBytes(StandardCharsets.UTF_8),
                    "application/json; charset=utf-8");
        } catch (RuntimeException exception) {
            throw new SerializationException("Unable to serialize request body", exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] value, TypeRef<T> type, String contentType) {
        if (type.type() == Void.class || type.type() == Void.TYPE || value.length == 0) {
            return null;
        }
        if (type.type() == byte[].class) {
            return (T) value;
        }
        String text = new String(value, StandardCharsets.UTF_8);
        if (type.type() == String.class && !isJson(contentType)) {
            return (T) text;
        }
        try {
            return gson.fromJson(text, type.type());
        } catch (JsonParseException exception) {
            throw new SerializationException("Unable to deserialize response body as "
                    + type.type().getTypeName(), exception);
        }
    }

    private static boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).contains("json");
    }
}
