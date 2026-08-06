package com.alness.universalrestclient.internal.okhttp;

import com.alness.universalrestclient.api.HttpBodies;
import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.RestClientConfig;
import com.alness.universalrestclient.config.JacksonBodyCodec;
import com.alness.universalrestclient.config.HttpErrorPolicy;
import com.alness.universalrestclient.exception.FailureType;
import com.alness.universalrestclient.exception.HttpStatusException;
import com.alness.universalrestclient.exception.ResponseTooLargeException;
import com.alness.universalrestclient.exception.SerializationException;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;
import com.alness.universalrestclient.exception.TransportException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OkHttpRestClientTest {
    private MockWebServer server;
    private OkHttpRestClient client;

    @BeforeEach
    void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
        client = OkHttpRestClient.builder().build();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.shutdown();
    }

    @Test
    void sendsSafeQueriesAndMultipleHeadersAndPreservesTheResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("X-Result", "one")
                .addHeader("X-Result", "two")
                .setBody("ready"));
        HttpRequest request = request(HttpMethod.GET, "/search").toBuilder()
                .headers(HttpHeaders.builder().add("X-Input", "one").add("X-Input", "two").build())
                .addQueryParameter("term", "a value & more")
                .build();

        HttpResponse<String> response = client.execute(request, TypeRef.of(String.class));
        RecordedRequest recorded = server.takeRequest();

        assertThat(recorded.getRequestUrl().queryParameter("term")).isEqualTo("a value & more");
        assertThat(recorded.getHeaders().values("X-Input")).containsExactly("one", "two");
        assertThat(response.body()).isEqualTo("ready");
        assertThat(response.headers().values("x-result")).containsExactly("one", "two");
        assertThat(response.rawBody()).containsExactly("ready".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void supportsEveryRequiredHttpMethod() throws Exception {
        List<HttpMethod> methods = Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
                HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS);

        for (HttpMethod method : methods) {
            MockResponse response = new MockResponse().setResponseCode(200);
            if (method != HttpMethod.HEAD) {
                response.setBody("ok");
            }
            server.enqueue(response);
            HttpRequest.Builder request = request(method, "/methods").toBuilder();
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                request.body(HttpBodies.text("payload"));
            }
            client.execute(request.build(), TypeRef.of(byte[].class));
            assertThat(server.takeRequest().getMethod()).isEqualTo(method.name());
        }
    }

    @Test
    void writesTextJsonBytesAndStreamBodies() throws Exception {
        executeAndAssertBody(HttpBodies.text("text"), "text", "text/plain");
        executeAndAssertBody(HttpBodies.json("{\"ok\":true}"), "{\"ok\":true}", "application/json");
        executeAndAssertBody(HttpBodies.bytes(new byte[] {0, 1, 2}), "000102", "application/octet-stream");
        executeAndAssertBody(HttpBodies.stream(
                        () -> new ByteArrayInputStream("stream".getBytes(StandardCharsets.UTF_8)),
                        6,
                        "application/custom"),
                "stream",
                "application/custom");
    }

    @Test
    void acceptsEmptyAndNoContentResponses() {
        server.enqueue(new MockResponse().setResponseCode(204));

        HttpResponse<Void> response = client.execute(request(HttpMethod.DELETE, "/empty"),
                TypeRef.of(Void.class));

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNull();
        assertThat(response.rawBody()).isEmpty();
    }

    @Test
    void cancelsAnExecutingCall() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("late")
                .setBodyDelay(5, TimeUnit.SECONDS));
        RestCall<String> call = client.newCall(request(HttpMethod.GET, "/cancel"),
                TypeRef.of(String.class));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<HttpResponse<String>> future = executor.submit(call::execute);
            assertThat(server.takeRequest(2, TimeUnit.SECONDS)).isNotNull();

            call.cancel();

            assertThatThrownBy(() -> get(future))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(TransportException.class);
            assertThat(call.isCanceled()).isTrue();
            assertThat(call.isExecuted()).isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void appliesReadTimeouts() {
        client = OkHttpRestClient.builder()
                .config(RestClientConfig.builder().readTimeoutMillis(100).build())
                .build();
        server.enqueue(new MockResponse().setBody("slow").setBodyDelay(1, TimeUnit.SECONDS));

        assertThatThrownBy(() -> client.execute(request(HttpMethod.GET, "/timeout"),
                TypeRef.of(String.class)))
                .isInstanceOf(TransportException.class)
                .matches(error -> ((TransportException) error).isRetryable());
    }

    @Test
    void reusesTheConnectionPoolAcrossRequests() throws Exception {
        server.enqueue(new MockResponse().setBody("one"));
        server.enqueue(new MockResponse().setBody("two"));

        client.execute(request(HttpMethod.GET, "/one"), TypeRef.of(String.class));
        client.execute(request(HttpMethod.GET, "/two"), TypeRef.of(String.class));

        assertThat(client.okHttpClient().connectionPool().connectionCount()).isEqualTo(1);
    }

    @Test
    void executesCallsConcurrentlyWithOneClientInstance() throws Exception {
        int requestCount = 12;
        for (int index = 0; index < requestCount; index++) {
            server.enqueue(new MockResponse().setBody("ok"));
        }
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<HttpResponse<String>>> futures =
                    new ArrayList<Future<HttpResponse<String>>>();
            for (int index = 0; index < requestCount; index++) {
                futures.add(executor.submit(() -> client.execute(
                        request(HttpMethod.GET, "/concurrent"), TypeRef.of(String.class))));
            }
            for (Future<HttpResponse<String>> future : futures) {
                assertThat(future.get(2, TimeUnit.SECONDS).body()).isEqualTo("ok");
            }
            assertThat(server.getRequestCount()).isEqualTo(requestCount);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void serializesTypedJsonAndDeserializesGenericJsonThroughTheTransport() throws Exception {
        client = OkHttpRestClient.builder().bodyCodec(JacksonBodyCodec.withDefaults()).build();
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("{\"values\":[1,2,3]}"));
        Map<String, String> requestValue = new LinkedHashMap<String, String>();
        requestValue.put("name", "client");

        HttpResponse<Map<String, List<Integer>>> response = client.execute(
                request(HttpMethod.POST, "/json").toBuilder()
                        .body(requestValue, new TypeRef<Map<String, String>>() { })
                        .build(),
                new TypeRef<Map<String, List<Integer>>>() { });

        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("{\"name\":\"client\"}");
        assertThat(response.body().get("values")).containsExactly(1, 2, 3);
    }

    @Test
    void returnsErrorResponsesWhenConfiguredToDoSo() {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("missing"));

        HttpResponse<String> response = client.execute(request(HttpMethod.GET, "/missing"),
                TypeRef.of(String.class));

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).isEqualTo("missing");
    }

    @Test
    void throwsStructuredSanitizedHttpErrorsWhenConfigured() {
        client = OkHttpRestClient.builder().config(RestClientConfig.builder()
                .errorPolicy(HttpErrorPolicy.THROW_ON_4XX_5XX).build()).build();
        server.enqueue(new MockResponse().setResponseCode(401)
                .addHeader("Set-Cookie", "session=secret")
                .setBody("unauthorized"));
        HttpRequest request = request(HttpMethod.GET, "/protected").toBuilder()
                .addQueryParameter("token", "supersecret")
                .build();

        assertThatThrownBy(() -> client.execute(request, TypeRef.of(String.class)))
                .isInstanceOfSatisfying(HttpStatusException.class, exception -> {
                    assertThat(exception.failureType()).isEqualTo(FailureType.HTTP_STATUS);
                    assertThat(exception.method()).isEqualTo(HttpMethod.GET);
                    assertThat(exception.statusCode()).isEqualTo(401);
                    assertThat(exception.uri().toString()).doesNotContain("supersecret");
                    assertThat(exception.headers().firstValue("Set-Cookie"))
                            .contains(SensitiveDataSanitizer.REDACTED);
                    assertThat(new String(exception.responseBody(), StandardCharsets.UTF_8))
                            .isEqualTo("unauthorized");
                    assertThat(exception.isRetryable()).isFalse();
                });
    }

    @Test
    void marksTemporaryHttpErrorsAsRetryable() {
        client = OkHttpRestClient.builder().config(RestClientConfig.builder()
                .errorPolicy(HttpErrorPolicy.THROW_ON_4XX_5XX).build()).build();
        server.enqueue(new MockResponse().setResponseCode(503));

        assertThatThrownBy(() -> client.execute(request(HttpMethod.GET, "/temporary"),
                TypeRef.of(String.class)))
                .isInstanceOfSatisfying(HttpStatusException.class,
                        exception -> assertThat(exception.isRetryable()).isTrue());
    }

    @Test
    void rejectsResponsesThatExceedTheConfiguredLimit() {
        client = OkHttpRestClient.builder().config(RestClientConfig.builder()
                .maxResponseBytes(4).build()).build();
        server.enqueue(new MockResponse().setBody("too-large"));

        assertThatThrownBy(() -> client.execute(request(HttpMethod.GET, "/large"),
                TypeRef.of(byte[].class)))
                .isInstanceOfSatisfying(ResponseTooLargeException.class, exception -> {
                    assertThat(exception.failureType()).isEqualTo(FailureType.RESPONSE_TOO_LARGE);
                    assertThat(exception.maximumBytes()).isEqualTo(4);
                    assertThat(exception.isRetryable()).isFalse();
                });
    }

    @Test
    void enrichesSerializationFailuresWithHttpContext() {
        client = OkHttpRestClient.builder().bodyCodec(JacksonBodyCodec.withDefaults()).build();
        server.enqueue(new MockResponse().addHeader("Content-Type", "application/json")
                .setBody("not-json"));

        assertThatThrownBy(() -> client.execute(request(HttpMethod.GET, "/invalid-json"),
                TypeRef.of(Map.class)))
                .isInstanceOfSatisfying(SerializationException.class, exception -> {
                    assertThat(exception.failureType()).isEqualTo(FailureType.SERIALIZATION);
                    assertThat(exception.statusCode()).isEqualTo(200);
                    assertThat(exception.method()).isEqualTo(HttpMethod.GET);
                    assertThat(exception.responseBody()).isNotEmpty();
                });
    }

    private HttpRequest request(HttpMethod method, String path) {
        return HttpRequest.builder().method(method).uri(server.url(path).toString()).build();
    }

    private void executeAndAssertBody(com.alness.universalrestclient.api.HttpBody body,
                                      String expected,
                                      String contentType) throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));
        client.execute(request(HttpMethod.POST, "/body").toBuilder().body(body).build(),
                TypeRef.of(String.class));
        RecordedRequest recorded = server.takeRequest();
        if (body.contentLength() == 3 && "application/octet-stream".equals(contentType)) {
            assertThat(recorded.getBody().readByteArray()).containsExactly(0, 1, 2);
        } else {
            assertThat(recorded.getBody().readUtf8()).isEqualTo(expected);
        }
        assertThat(recorded.getHeader("Content-Type")).startsWith(contentType);
    }

    private static <T> T get(Future<T> future) throws Exception {
        return future.get(2, TimeUnit.SECONDS);
    }
}
