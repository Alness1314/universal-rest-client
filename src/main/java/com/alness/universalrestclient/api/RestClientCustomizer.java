package com.alness.universalrestclient.api;

import com.alness.universalrestclient.config.RestClientBuilder;

/** Framework-neutral hook suitable for Spring beans, CDI producers or manual wiring. */
public interface RestClientCustomizer {
    void customize(RestClientBuilder builder);
}
