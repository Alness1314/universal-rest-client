package com.alness.universalrestclient.internal.retry;

/** Injectable wait operation used to keep retry tests deterministic. */
interface Sleeper {
    void sleep(long millis) throws InterruptedException;
}
