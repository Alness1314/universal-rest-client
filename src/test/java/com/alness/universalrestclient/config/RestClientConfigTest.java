package com.alness.universalrestclient.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestClientConfigTest {
    @Test
    void exposesSafeDefaults() {
        RestClientConfig config = RestClientConfig.defaults();

        assertThat(config.connectTimeoutMillis()).isEqualTo(5_000L);
        assertThat(config.readTimeoutMillis()).isEqualTo(10_000L);
        assertThat(config.followRedirects()).isTrue();
    }

    @Test
    void validatesPositiveValues() {
        assertThatThrownBy(() -> RestClientConfig.builder().connectTimeoutMillis(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createsIndependentModifiedCopies() {
        RestClientConfig original = RestClientConfig.defaults();
        RestClientConfig modified = original.toBuilder().followRedirects(false).build();

        assertThat(original.followRedirects()).isTrue();
        assertThat(modified.followRedirects()).isFalse();
    }
}
