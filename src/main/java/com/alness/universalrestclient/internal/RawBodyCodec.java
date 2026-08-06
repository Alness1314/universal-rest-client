package com.alness.universalrestclient.internal;

import com.alness.universalrestclient.api.BodyCodec;
import com.alness.universalrestclient.api.HttpBodies;
import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.SerializationException;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Built-in codec for raw strings, bytes and empty responses. */
public final class RawBodyCodec implements BodyCodec {
    @Override
    public HttpBody serialize(Object value, TypeRef<?> type) {
        if (value instanceof HttpBody) {
            return (HttpBody) value;
        }
        if (value instanceof byte[]) {
            return HttpBodies.bytes((byte[]) value);
        }
        if (value instanceof String) {
            return HttpBodies.text((String) value);
        }
        throw unsupported(type.type());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] value, TypeRef<T> type, String contentType) {
        Type requestedType = type.type();
        if (requestedType == Void.class || requestedType == Void.TYPE) {
            return null;
        }
        if (requestedType == byte[].class) {
            return (T) value;
        }
        if (requestedType == String.class) {
            return (T) new String(value, charset(contentType));
        }
        throw unsupported(requestedType);
    }

    private static Charset charset(String contentType) {
        if (contentType != null) {
            String lower = contentType.toLowerCase(java.util.Locale.ROOT);
            int index = lower.indexOf("charset=");
            if (index >= 0) {
                String charset = contentType.substring(index + 8).trim().replace("\"", "");
                try {
                    return Charset.forName(charset);
                } catch (IllegalArgumentException ignored) {
                    return StandardCharsets.UTF_8;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static SerializationException unsupported(Type type) {
        return new SerializationException("No codec is registered for " + type.getTypeName(), null);
    }
}
