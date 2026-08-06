package com.alness.universalrestclient.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {
    @Test
    void isDisabledByDefault() {
        assertThat(RetryPolicy.disabled().maxAttempts()).isEqualTo(1);
    }

    @Test
    void validatesBounds() {
        assertThatThrownBy(() -> RetryPolicy.builder().maxAttempts(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RetryPolicy.builder().jitterFactor(1.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RetryPolicy.builder()
                .initialDelayMillis(200).maxDelayMillis(100).build())
                .isInstanceOf(IllegalStateException.class);
    }
}
