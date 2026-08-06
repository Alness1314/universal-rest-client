package com.alness.universalrestclient.api;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpRequestTest {
    @Test
    void buildsAnImmutableRequest() throws Exception {
        byte[] source = "body".getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .uri("https://example.test/resource")
                .addQueryParameter("tag", "one")
                .addQueryParameter("tag", "two")
                .body(source)
                .build();

        source[0] = 'X';

        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.queryParameters().get("tag")).containsExactly("one", "two");
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        request.body().writeTo(body);
        assertThat(new String(body.toByteArray(), StandardCharsets.UTF_8)).isEqualTo("body");
    }

    @Test
    void rejectsRelativeAndNonHttpUris() {
        assertThatThrownBy(() -> HttpRequest.builder()
                .method(HttpMethod.GET).uri("/relative").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> HttpRequest.builder()
                .method(HttpMethod.GET).uri("file:///tmp/data").build())
                .isInstanceOf(IllegalStateException.class);
    }
}
