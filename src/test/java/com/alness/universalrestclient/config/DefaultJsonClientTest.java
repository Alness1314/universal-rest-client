package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TypeRef;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultJsonClientTest {
    @Test
    void deserializesJsonWithoutExplicitCodecConfiguration() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"id\":25,\"name\":\"pikachu\"}"));
            RestClient client = RestClients.create();
            HttpRequest request = HttpRequest.builder()
                    .method(HttpMethod.GET)
                    .uri(server.url("/pokemon/pikachu").toString())
                    .build();

            HttpResponse<Pokemon> response = client.execute(
                    request, TypeRef.of(Pokemon.class));

            assertThat(response.body().id).isEqualTo(25);
            assertThat(response.body().name).isEqualTo("pikachu");
        }
    }

    public static final class Pokemon {
        public int id;
        public String name;
    }
}
