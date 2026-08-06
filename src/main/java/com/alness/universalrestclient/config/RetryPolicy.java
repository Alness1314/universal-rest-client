package com.alness.universalrestclient.config;

/** Immutable exponential-backoff retry configuration. */
public final class RetryPolicy {
    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long maxDelayMillis;
    private final double jitterFactor;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelayMillis = builder.initialDelayMillis;
        this.maxDelayMillis = builder.maxDelayMillis;
        this.jitterFactor = builder.jitterFactor;
    }

    public static RetryPolicy disabled() {
        return builder().maxAttempts(1).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public long initialDelayMillis() {
        return initialDelayMillis;
    }

    public long maxDelayMillis() {
        return maxDelayMillis;
    }

    public double jitterFactor() {
        return jitterFactor;
    }

    /** Builder for bounded retry settings. */
    public static final class Builder {
        private int maxAttempts = 1;
        private long initialDelayMillis = 100;
        private long maxDelayMillis = 2_000;
        private double jitterFactor = 0.2;

        public Builder maxAttempts(int value) {
            if (value < 1) {
                throw new IllegalArgumentException("maxAttempts must be at least one");
            }
            this.maxAttempts = value;
            return this;
        }

        public Builder initialDelayMillis(long value) {
            if (value < 0) {
                throw new IllegalArgumentException("initialDelayMillis must not be negative");
            }
            this.initialDelayMillis = value;
            return this;
        }

        public Builder maxDelayMillis(long value) {
            if (value < 0) {
                throw new IllegalArgumentException("maxDelayMillis must not be negative");
            }
            this.maxDelayMillis = value;
            return this;
        }

        public Builder jitterFactor(double value) {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("jitterFactor must be between zero and one");
            }
            this.jitterFactor = value;
            return this;
        }

        public RetryPolicy build() {
            if (maxDelayMillis < initialDelayMillis) {
                throw new IllegalStateException("maxDelayMillis must be at least initialDelayMillis");
            }
            return new RetryPolicy(this);
        }
    }
}
