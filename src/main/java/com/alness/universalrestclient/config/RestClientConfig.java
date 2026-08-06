package com.alness.universalrestclient.config;

/** Immutable configuration shared by transport implementations. */
public final class RestClientConfig {
    public static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 5_000L;
    public static final long DEFAULT_READ_TIMEOUT_MILLIS = 10_000L;
    public static final long DEFAULT_WRITE_TIMEOUT_MILLIS = 10_000L;
    public static final long DEFAULT_MAX_RESPONSE_BYTES = 10L * 1024L * 1024L;

    private final long connectTimeoutMillis;
    private final long readTimeoutMillis;
    private final long writeTimeoutMillis;
    private final long maxResponseBytes;
    private final boolean followRedirects;
    private final HttpErrorPolicy errorPolicy;

    private RestClientConfig(Builder builder) {
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.writeTimeoutMillis = builder.writeTimeoutMillis;
        this.maxResponseBytes = builder.maxResponseBytes;
        this.followRedirects = builder.followRedirects;
        this.errorPolicy = builder.errorPolicy;
    }

    public static RestClientConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public long connectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public long readTimeoutMillis() {
        return readTimeoutMillis;
    }

    public long writeTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public long maxResponseBytes() {
        return maxResponseBytes;
    }

    public boolean followRedirects() {
        return followRedirects;
    }

    public HttpErrorPolicy errorPolicy() {
        return errorPolicy;
    }

    public Builder toBuilder() {
        return new Builder()
                .connectTimeoutMillis(connectTimeoutMillis)
                .readTimeoutMillis(readTimeoutMillis)
                .writeTimeoutMillis(writeTimeoutMillis)
                .maxResponseBytes(maxResponseBytes)
                .followRedirects(followRedirects)
                .errorPolicy(errorPolicy);
    }

    /** Builder for validated client configuration. */
    public static final class Builder {
        private long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        private long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
        private long writeTimeoutMillis = DEFAULT_WRITE_TIMEOUT_MILLIS;
        private long maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;
        private boolean followRedirects = true;
        private HttpErrorPolicy errorPolicy = HttpErrorPolicy.RETURN_RESPONSE;

        public Builder connectTimeoutMillis(long value) {
            this.connectTimeoutMillis = positive(value, "connectTimeoutMillis");
            return this;
        }

        public Builder readTimeoutMillis(long value) {
            this.readTimeoutMillis = positive(value, "readTimeoutMillis");
            return this;
        }

        public Builder writeTimeoutMillis(long value) {
            this.writeTimeoutMillis = positive(value, "writeTimeoutMillis");
            return this;
        }

        public Builder maxResponseBytes(long value) {
            this.maxResponseBytes = positive(value, "maxResponseBytes");
            return this;
        }

        public Builder followRedirects(boolean value) {
            this.followRedirects = value;
            return this;
        }

        public Builder errorPolicy(HttpErrorPolicy value) {
            this.errorPolicy = java.util.Objects.requireNonNull(value,
                    "errorPolicy must not be null");
            return this;
        }

        public RestClientConfig build() {
            return new RestClientConfig(this);
        }

        private static long positive(long value, String field) {
            if (value <= 0) {
                throw new IllegalArgumentException(field + " must be greater than zero");
            }
            return value;
        }
    }
}
