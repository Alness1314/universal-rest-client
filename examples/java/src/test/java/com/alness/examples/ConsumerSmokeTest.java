package com.alness.examples;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.RestClientBuilder;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsumerSmokeTest {
    @Test
    void consumesTheInstalledLibraryFromAnIndependentMavenProject() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/health", exchange -> {
            byte[] body = "ready".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            RestClient client = RestClientBuilder.builder().build();
            HttpRequest request = HttpRequest.builder().method(HttpMethod.GET)
                    .uri("http://127.0.0.1:" + server.getAddress().getPort() + "/health")
                    .build();

            HttpResponse<String> response = client.execute(request, TypeRef.of(String.class));

            assertEquals(200, response.statusCode());
            assertEquals("ready", response.body());
        } finally {
            server.stop(0);
        }
    }
}
