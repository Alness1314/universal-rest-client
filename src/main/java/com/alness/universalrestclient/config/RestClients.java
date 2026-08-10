package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.RestClient;

/** Convenience entry point for the ready-to-use client. */
public final class RestClients {
    private RestClients() {
    }

    /** Creates a client with JSON, sensible timeouts and asynchronous support. */
    public static RestClient create() {
        return builder().build();
    }

    /** Creates a builder whose default codec supports JSON through Jackson. */
    public static RestClientBuilder builder() {
        return RestClientBuilder.builder();
    }
}
