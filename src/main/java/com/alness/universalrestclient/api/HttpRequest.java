package com.alness.universalrestclient.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable transport-neutral HTTP request. */
public final class HttpRequest {
    private final HttpMethod method;
    private final URI uri;
    private final HttpHeaders headers;
    private final Map<String, List<String>> queryParameters;
    private final HttpBody body;
    private final Object bodyValue;
    private final TypeRef<?> bodyType;
    private final RetryMode retryMode;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = builder.headers;
        this.queryParameters = immutableParameters(builder.queryParameters);
        this.body = builder.body;
        this.bodyValue = builder.bodyValue;
        this.bodyType = builder.bodyType;
        this.retryMode = builder.retryMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HttpMethod method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public Map<String, List<String>> queryParameters() {
        return queryParameters;
    }

    public HttpBody body() {
        return body;
    }

    public boolean hasBody() {
        return body != null || bodyType != null;
    }

    public boolean hasTypedBody() {
        return bodyType != null;
    }

    public Object bodyValue() {
        return bodyValue;
    }

    public TypeRef<?> bodyType() {
        return bodyType;
    }

    public RetryMode retryMode() {
        return retryMode;
    }

    public Builder toBuilder() {
        Builder builder = new Builder()
                .method(method)
                .uri(uri)
                .headers(headers)
                .retryMode(retryMode);
        if (bodyType != null) {
            builder.body(bodyValue, bodyType);
        } else {
            builder.body(body);
        }
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            for (String value : entry.getValue()) {
                builder.addQueryParameter(entry.getKey(), value);
            }
        }
        return builder;
    }

    private static Map<String, List<String>> immutableParameters(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    /** Builder for validated immutable requests. */
    public static final class Builder {
        private HttpMethod method;
        private URI uri;
        private HttpHeaders headers = HttpHeaders.empty();
        private final Map<String, List<String>> queryParameters =
                new LinkedHashMap<String, List<String>>();
        private HttpBody body;
        private Object bodyValue;
        private TypeRef<?> bodyType;
        private RetryMode retryMode = RetryMode.DEFAULT;

        public Builder method(HttpMethod method) {
            this.method = Objects.requireNonNull(method, "method must not be null");
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = Objects.requireNonNull(uri, "uri must not be null");
            return this;
        }

        public Builder uri(String uri) {
            Objects.requireNonNull(uri, "uri must not be null");
            this.uri = URI.create(uri);
            return this;
        }

        public Builder headers(HttpHeaders headers) {
            this.headers = Objects.requireNonNull(headers, "headers must not be null");
            return this;
        }

        public Builder addQueryParameter(String name, String value) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("query parameter name must not be blank");
            }
            Objects.requireNonNull(value, "query parameter value must not be null");
            List<String> values = queryParameters.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                queryParameters.put(name, values);
            }
            values.add(value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body == null ? null : HttpBodies.bytes(body);
            this.bodyValue = null;
            this.bodyType = null;
            return this;
        }

        public Builder body(HttpBody body) {
            this.body = body;
            this.bodyValue = null;
            this.bodyType = null;
            return this;
        }

        public Builder body(Object value, Class<?> type) {
            return body(value, TypeRef.of(type));
        }

        public Builder body(Object value, TypeRef<?> type) {
            this.body = null;
            this.bodyValue = value;
            this.bodyType = Objects.requireNonNull(type, "body type must not be null");
            return this;
        }

        public Builder retryMode(RetryMode retryMode) {
            this.retryMode = Objects.requireNonNull(retryMode, "retryMode must not be null");
            return this;
        }

        public HttpRequest build() {
            if (method == null) {
                throw new IllegalStateException("method must be configured");
            }
            if (uri == null) {
                throw new IllegalStateException("uri must be configured");
            }
            if (!uri.isAbsolute() || uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException("uri must be an absolute HTTP or HTTPS URI");
            }
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalStateException("uri scheme must be HTTP or HTTPS");
            }
            return new HttpRequest(this);
        }
    }
}
