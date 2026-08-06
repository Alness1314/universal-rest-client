package com.alness.universalrestclient.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/** Factory methods for transport-neutral request bodies. */
public final class HttpBodies {
    private static final int BUFFER_SIZE = 8_192;

    private HttpBodies() {
    }

    public static HttpBody text(String value) {
        return text(value, StandardCharsets.UTF_8, "text/plain; charset=utf-8");
    }

    public static HttpBody text(String value, Charset charset, String contentType) {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        return bytes(value.getBytes(charset), contentType);
    }

    public static HttpBody json(String json) {
        Objects.requireNonNull(json, "json must not be null");
        return bytes(json.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
    }

    public static HttpBody bytes(byte[] value) {
        return bytes(value, "application/octet-stream");
    }

    public static HttpBody bytes(byte[] value, String contentType) {
        Objects.requireNonNull(value, "value must not be null");
        return new ByteArrayBody(value, contentType);
    }

    public static HttpBody stream(Supplier<? extends InputStream> source,
                                  long contentLength,
                                  String contentType) {
        Objects.requireNonNull(source, "source must not be null");
        if (contentLength < -1) {
            throw new IllegalArgumentException("contentLength must be -1 or greater");
        }
        return new StreamBody(source, contentLength, contentType);
    }

    private static final class ByteArrayBody implements HttpBody {
        private final byte[] value;
        private final String contentType;

        private ByteArrayBody(byte[] value, String contentType) {
            this.value = Arrays.copyOf(value, value.length);
            this.contentType = contentType;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return value.length;
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public void writeTo(OutputStream output) throws IOException {
            output.write(value);
        }
    }

    private static final class StreamBody implements HttpBody {
        private final Supplier<? extends InputStream> source;
        private final long contentLength;
        private final String contentType;

        private StreamBody(Supplier<? extends InputStream> source,
                           long contentLength,
                           String contentType) {
            this.source = source;
            this.contentLength = contentLength;
            this.contentType = contentType;
        }

        @Override
        public String contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public void writeTo(OutputStream output) throws IOException {
            InputStream input = source.get();
            if (input == null) {
                throw new IOException("stream supplier returned null");
            }
            try (InputStream closeable = input) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = closeable.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
        }
    }
}
