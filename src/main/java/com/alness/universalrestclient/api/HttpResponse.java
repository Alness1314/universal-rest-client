package com.alness.universalrestclient.api;

import java.util.Arrays;

/** Immutable HTTP response preserving both the decoded and original body. */
public final class HttpResponse<T> {
    private final int statusCode;
    private final HttpHeaders headers;
    private final T body;
    private final byte[] rawBody;

    private HttpResponse(Builder<T> builder) {
        this.statusCode = builder.statusCode;
        this.headers = builder.headers;
        this.body = builder.body;
        this.rawBody = Arrays.copyOf(builder.rawBody, builder.rawBody.length);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public int statusCode() {
        return statusCode;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public T body() {
        return body;
    }

    public byte[] rawBody() {
        return Arrays.copyOf(rawBody, rawBody.length);
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Builder<T> toBuilder() {
        return HttpResponse.<T>builder()
                .statusCode(statusCode)
                .headers(headers)
                .body(body)
                .rawBody(rawBody);
    }

    /** Builder for immutable HTTP responses. */
    public static final class Builder<T> {
        private int statusCode;
        private HttpHeaders headers = HttpHeaders.empty();
        private T body;
        private byte[] rawBody = new byte[0];

        public Builder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder<T> headers(HttpHeaders headers) {
            if (headers == null) {
                throw new IllegalArgumentException("headers must not be null");
            }
            this.headers = headers;
            return this;
        }

        public Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        public Builder<T> rawBody(byte[] rawBody) {
            this.rawBody = rawBody == null ? new byte[0] : Arrays.copyOf(rawBody, rawBody.length);
            return this;
        }

        public HttpResponse<T> build() {
            if (statusCode < 100 || statusCode > 599) {
                throw new IllegalStateException("status code must be between 100 and 599");
            }
            return new HttpResponse<T>(this);
        }
    }
}
