package com.alness.universalrestclient.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/** Captures generic response type information without depending on a JSON library. */
public abstract class TypeRef<T> {
    private final Type type;

    protected TypeRef() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalStateException("TypeRef must be created with generic type information");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    private TypeRef(Type type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public static <T> TypeRef<T> of(final Class<T> type) {
        return new DirectTypeRef<T>(type);
    }

    public final Type type() {
        return type;
    }

    @Override
    public final String toString() {
        return type.getTypeName();
    }

    private static final class DirectTypeRef<T> extends TypeRef<T> {
        private DirectTypeRef(Type type) {
            super(type);
        }
    }
}
