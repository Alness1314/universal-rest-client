package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.interceptor.ApiKeyInterceptor;
import com.alness.universalrestclient.config.interceptor.AuthenticationResponseInterceptor;
import com.alness.universalrestclient.config.interceptor.BearerTokenInterceptor;
import com.alness.universalrestclient.config.interceptor.CommonHeadersInterceptor;
import com.alness.universalrestclient.config.interceptor.CorrelationIdInterceptor;
import com.alness.universalrestclient.config.interceptor.UserAgentInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class InterceptorsIntegrationTest {
    private MockWebServer server;

    @BeforeEach
    void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.shutdown();
    }

    @Test
    void appliesDynamicAuthenticationAndCommonMetadataInOrder() throws Exception {
        AtomicReference<String> token = new AtomicReference<String>("first-token");
        RestClient client = RestClientBuilder.builder()
                .addRequestInterceptor(new CommonHeadersInterceptor(HttpHeaders.builder()
                        .set("Accept-Language", "es-MX").set("X-Override", "common").build()))
                .addRequestInterceptor(new BearerTokenInterceptor(token::get))
                .addRequestInterceptor(new ApiKeyInterceptor("X-API-Key", () -> "api-secret"))
                .addRequestInterceptor(new CorrelationIdInterceptor("X-Trace-ID", () -> "trace-1"))
                .addRequestInterceptor(new UserAgentInterceptor("universal-rest-client/test"))
                .build();
        server.enqueue(new MockResponse().setBody("one"));
        server.enqueue(new MockResponse().setBody("two"));
        HttpRequest request = request().toBuilder().headers(HttpHeaders.builder()
                .set("X-Override", "request").build()).build();

        client.execute(request, TypeRef.of(String.class));
        token.set("Bearer second-token");
        client.execute(request, TypeRef.of(String.class));
        RecordedRequest first = server.takeRequest();
        RecordedRequest second = server.takeRequest();

        assertThat(first.getHeader("Authorization")).isEqualTo("Bearer first-token");
        assertThat(second.getHeader("Authorization")).isEqualTo("Bearer second-token");
        assertThat(first.getHeader("X-API-Key")).isEqualTo("api-secret");
        assertThat(first.getHeader("X-Trace-ID")).isEqualTo("trace-1");
        assertThat(first.getHeader("Accept-Language")).isEqualTo("es-MX");
        assertThat(first.getHeader("X-Override")).isEqualTo("request");
        assertThat(first.getHeader("User-Agent")).isEqualTo("universal-rest-client/test");
    }

    @Test
    void transformsResponsesAndNotifiesAuthenticationListener() {
        AtomicInteger unauthorized = new AtomicInteger();
        RestClient client = RestClientBuilder.builder()
                .addResponseInterceptor((request, response) -> response.toBuilder()
                        .headers(response.headers().toBuilder().set("X-Intercepted", "yes").build())
                        .build())
                .addResponseInterceptor(new AuthenticationResponseInterceptor(
                        (request, response) -> unauthorized.incrementAndGet()))
                .build();
        server.enqueue(new MockResponse().setResponseCode(401).setBody("unauthorized"));

        HttpResponse<String> response = client.execute(request(), TypeRef.of(String.class));

        assertThat(response.headers().firstValue("X-Intercepted")).contains("yes");
        assertThat(unauthorized).hasValue(1);
    }

    @Test
    void logsMetadataWithoutSecretsOrBodies() {
        Logger logger = Logger.getLogger("universal-rest-client-test-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        CapturingHandler handler = new CapturingHandler();
        logger.addHandler(handler);
        RestClient client = RestClientBuilder.builder()
                .addRequestInterceptor(new BearerTokenInterceptor(() -> "very-secret-token"))
                .addObserver(new SafeLoggingObserver(logger, Level.INFO))
                .build();
        server.enqueue(new MockResponse().setBody("sensitive-response-body"));

        client.execute(request(), TypeRef.of(String.class));

        assertThat(handler.messages.toString())
                .contains("[REDACTED]")
                .doesNotContain("very-secret-token")
                .doesNotContain("sensitive-response-body");
    }

    private HttpRequest request() {
        return HttpRequest.builder().method(HttpMethod.GET)
                .uri(server.url("/interceptors").toString()).build();
    }

    private static final class CapturingHandler extends Handler {
        private final StringBuilder messages = new StringBuilder();

        @Override
        public void publish(LogRecord record) {
            messages.append(java.text.MessageFormat.format(record.getMessage(),
                    record.getParameters())).append('\n');
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
