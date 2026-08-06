package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.TypeRef;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GsonBodyCodecTest {
    @Test
    void supportsInjectedGsonAndGenericTypes() {
        Gson gson = new Gson();
        GsonBodyCodec codec = new GsonBodyCodec(gson);

        Map<String, List<Integer>> result = codec.deserialize(
                "{\"values\":[1,2]}".getBytes(StandardCharsets.UTF_8),
                new TypeRef<Map<String, List<Integer>>>() { },
                "application/json");

        assertThat(codec.gson()).isSameAs(gson);
        assertThat(result.get("values")).containsExactly(1, 2);
    }

    @Test
    void handlesEmptyAndPlainTextResponses() {
        GsonBodyCodec codec = GsonBodyCodec.withDefaults();

        Map<?, ?> empty = codec.deserialize(new byte[0], TypeRef.of(Map.class),
                "application/json");
        String text = codec.deserialize("plain".getBytes(StandardCharsets.UTF_8),
                TypeRef.of(String.class), "text/plain");

        assertThat(empty).isNull();
        assertThat(text).isEqualTo("plain");
    }
}
