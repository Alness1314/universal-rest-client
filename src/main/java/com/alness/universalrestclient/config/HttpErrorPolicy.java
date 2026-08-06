package com.alness.universalrestclient.config;

/** Determines which HTTP status codes are converted into exceptions. */
public enum HttpErrorPolicy {
    RETURN_RESPONSE {
        @Override
        public boolean shouldThrow(int statusCode) {
            return false;
        }
    },
    THROW_ON_4XX_5XX {
        @Override
        public boolean shouldThrow(int statusCode) {
            return statusCode >= 400 && statusCode <= 599;
        }
    },
    THROW_ON_NON_SUCCESS {
        @Override
        public boolean shouldThrow(int statusCode) {
            return statusCode < 200 || statusCode >= 300;
        }
    };

    public abstract boolean shouldThrow(int statusCode);
}
