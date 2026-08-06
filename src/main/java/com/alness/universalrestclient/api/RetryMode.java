package com.alness.universalrestclient.api;

/** Per-request override for retry safety. */
public enum RetryMode {
    DEFAULT,
    ENABLED,
    DISABLED
}
