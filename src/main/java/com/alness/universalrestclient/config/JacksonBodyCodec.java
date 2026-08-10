package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.BodyCodec;
import com.alness.universalrestclient.api.HttpBodies;
import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.Objects;

/** Default JSON adapter backed by Jackson. */
public final class JacksonBodyCodec implements BodyCodec {
    private final ObjectMapper objectMapper;

    public JacksonBodyCodec(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public static JacksonBodyCodec withDefaults() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return new JacksonBodyCodec(mapper);
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Override
    public HttpBody serialize(Object value, TypeRef<?> type) {
        try {
            return HttpBodies.bytes(objectMapper.writerFor(javaType(type))
                    .writeValueAsBytes(value), "application/json; charset=utf-8");
        } catch (JsonProcessingException exception) {
            throw new SerializationException("Unable to serialize request body", exception);
        }
    }

    @Override
    public <T> T deserialize(byte[] value, TypeRef<T> type, String contentType) {
        if (type.type() == Void.class || type.type() == Void.TYPE || value.length == 0) {
            return null;
        }
        if (type.type() == String.class && !isJson(contentType)) {
            return new RawStringDecoder<T>().decode(value);
        }
        if (type.type() == byte[].class) {
            return new RawBytesDecoder<T>().decode(value);
        }
        try {
            return objectMapper.readValue(value, javaType(type));
        } catch (IOException exception) {
            throw new SerializationException("Unable to deserialize response body as "
                    + type.type().getTypeName(), exception);
        }
    }

    private JavaType javaType(TypeRef<?> type) {
        return objectMapper.getTypeFactory().constructType(type.type());
    }

    private static boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).contains("json");
    }

    private static final class RawStringDecoder<T> {
        @SuppressWarnings("unchecked")
        private T decode(byte[] value) {
            return (T) new String(value, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static final class RawBytesDecoder<T> {
        @SuppressWarnings("unchecked")
        private T decode(byte[] value) {
            return (T) value;
        }
    }
}
