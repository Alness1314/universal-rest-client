package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.SerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonBodyCodecTest {
    @Test
    void serializesDatesAndDeserializesGenericEnvelopes() throws Exception {
        JacksonBodyCodec codec = JacksonBodyCodec.withDefaults();
        Event source = new Event("release", LocalDate.of(2026, 8, 6));

        HttpBody body = codec.serialize(source, TypeRef.of(Event.class));
        Envelope<List<Event>> result = codec.deserialize(
                "{\"data\":[{\"name\":\"release\",\"date\":\"2026-08-06\"}]}"
                        .getBytes(StandardCharsets.UTF_8),
                new TypeRef<Envelope<List<Event>>>() { },
                "application/json");

        assertThat(read(body)).contains("\"date\":\"2026-08-06\"");
        assertThat(result.getData()).containsExactly(source);
    }

    @Test
    void supportsMapsListsAndAnInjectedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        JacksonBodyCodec codec = new JacksonBodyCodec(mapper);

        Map<String, List<Integer>> result = codec.deserialize(
                "{\"values\":[1,2]}".getBytes(StandardCharsets.UTF_8),
                new TypeRef<Map<String, List<Integer>>>() { },
                "application/json");

        assertThat(codec.objectMapper()).isSameAs(mapper);
        assertThat(result.get("values")).containsExactly(1, 2);
    }

    @Test
    void handlesEmptyAndNonJsonResponsesWithoutForcingJsonParsing() {
        JacksonBodyCodec codec = JacksonBodyCodec.withDefaults();

        Event empty = codec.deserialize(new byte[0], TypeRef.of(Event.class), "application/json");
        String text = codec.deserialize("plain".getBytes(StandardCharsets.UTF_8),
                TypeRef.of(String.class), "text/plain");

        assertThat(empty).isNull();
        assertThat(text).isEqualTo("plain");
    }

    @Test
    void reportsMalformedJsonAsSerializationFailure() {
        JacksonBodyCodec codec = JacksonBodyCodec.withDefaults();

        assertThatThrownBy(() -> codec.deserialize("not-json".getBytes(StandardCharsets.UTF_8),
                TypeRef.of(Event.class), "application/json"))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining(Event.class.getName());
    }

    private static String read(HttpBody body) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        body.writeTo(output);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

    public static final class Envelope<T> {
        private T data;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    public static final class Event {
        private String name;
        private LocalDate date;

        public Event() {
        }

        public Event(String name, LocalDate date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Event)) {
                return false;
            }
            Event event = (Event) other;
            return java.util.Objects.equals(name, event.name)
                    && java.util.Objects.equals(date, event.date);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, date);
        }
    }
}
