package com.alness.universalrestclient.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable, case-insensitive collection of HTTP headers. */
public final class HttpHeaders {
    private static final HttpHeaders EMPTY = new HttpHeaders(Collections.<String, HeaderValues>emptyMap());

    private final Map<String, HeaderValues> values;

    private HttpHeaders(Map<String, HeaderValues> values) {
        this.values = values;
    }

    public static HttpHeaders empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> firstValue(String name) {
        List<String> all = values(name);
        return all.isEmpty() ? Optional.<String>empty() : Optional.of(all.get(0));
    }

    public List<String> values(String name) {
        Objects.requireNonNull(name, "name must not be null");
        HeaderValues entry = values.get(normalize(name));
        return entry == null ? Collections.<String>emptyList() : entry.values;
    }

    public boolean contains(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return values.containsKey(normalize(name));
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Map<String, List<String>> asMap() {
        Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        for (HeaderValues entry : values.values()) {
            result.put(entry.originalName, entry.values);
        }
        return Collections.unmodifiableMap(result);
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        for (HeaderValues entry : values.values()) {
            builder.values.put(normalize(entry.originalName), entry.mutableCopy());
        }
        return builder;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("header name must not be blank");
        }
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (character <= 32 || character >= 127 || "()<>@,;:\\\"/[]?={}\t".indexOf(character) >= 0) {
                throw new IllegalArgumentException("invalid header name: " + name);
            }
        }
    }

    private static void validateValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("header value must not be null");
        }
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("header value must not contain line breaks");
        }
    }

    /** Mutable builder that creates immutable header collections. */
    public static final class Builder {
        private final Map<String, HeaderValues> values = new LinkedHashMap<String, HeaderValues>();

        public Builder add(String name, String value) {
            validateName(name);
            validateValue(value);
            String key = normalize(name);
            HeaderValues entry = values.get(key);
            if (entry == null) {
                entry = new HeaderValues(name, new ArrayList<String>());
                values.put(key, entry);
            }
            entry.values.add(value);
            return this;
        }

        public Builder set(String name, String value) {
            validateName(name);
            validateValue(value);
            List<String> newValues = new ArrayList<String>();
            newValues.add(value);
            values.put(normalize(name), new HeaderValues(name, newValues));
            return this;
        }

        public Builder remove(String name) {
            Objects.requireNonNull(name, "name must not be null");
            values.remove(normalize(name));
            return this;
        }

        public HttpHeaders build() {
            if (values.isEmpty()) {
                return EMPTY;
            }
            Map<String, HeaderValues> copy = new LinkedHashMap<String, HeaderValues>();
            for (Map.Entry<String, HeaderValues> entry : values.entrySet()) {
                copy.put(entry.getKey(), entry.getValue().immutableCopy());
            }
            return new HttpHeaders(Collections.unmodifiableMap(copy));
        }
    }

    private static final class HeaderValues {
        private final String originalName;
        private final List<String> values;

        private HeaderValues(String originalName, List<String> values) {
            this.originalName = originalName;
            this.values = values;
        }

        private HeaderValues immutableCopy() {
            return new HeaderValues(originalName,
                    Collections.unmodifiableList(new ArrayList<String>(values)));
        }

        private HeaderValues mutableCopy() {
            return new HeaderValues(originalName, new ArrayList<String>(values));
        }
    }
}
