package com.alness.universalrestclient.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpHeadersTest {
    @Test
    void treatsNamesAsCaseInsensitiveAndPreservesMultipleValues() {
        HttpHeaders headers = HttpHeaders.builder()
                .add("Accept", "application/json")
                .add("accept", "text/plain")
                .build();

        assertThat(headers.values("ACCEPT"))
                .containsExactly("application/json", "text/plain");
        assertThat(headers.asMap()).containsOnlyKeys("Accept");
    }

    @Test
    void rejectsHeaderInjection() {
        assertThatThrownBy(() -> HttpHeaders.builder().add("X-Test", "safe\r\nInjected: true"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void producesImmutableValues() {
        HttpHeaders headers = HttpHeaders.builder().add("Accept", "application/json").build();

        assertThatThrownBy(() -> headers.values("Accept").add("text/plain"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
