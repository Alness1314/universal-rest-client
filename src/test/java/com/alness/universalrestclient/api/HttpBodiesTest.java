package com.alness.universalrestclient.api;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HttpBodiesTest {
    @Test
    void createsJsonAndDefensivelyCopiedByteBodies() throws Exception {
        byte[] source = "original".getBytes(StandardCharsets.UTF_8);
        HttpBody bytes = HttpBodies.bytes(source);
        source[0] = 'X';

        assertThat(write(bytes)).isEqualTo("original");
        assertThat(bytes.isRepeatable()).isTrue();
        assertThat(HttpBodies.json("{}").contentType()).startsWith("application/json");
    }

    @Test
    void obtainsANewStreamWhenWritten() throws Exception {
        HttpBody stream = HttpBodies.stream(
                () -> new ByteArrayInputStream("stream".getBytes(StandardCharsets.UTF_8)),
                6,
                "application/octet-stream");

        assertThat(write(stream)).isEqualTo("stream");
        assertThat(write(stream)).isEqualTo("stream");
    }

    private static String write(HttpBody body) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        body.writeTo(output);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
