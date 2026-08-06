package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TraceContextProvider;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.interceptor.UserAgentInterceptor;
import com.alness.universalrestclient.config.interceptor.W3cTraceContextInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalIntegrationsTest {
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
    void appliesFrameworkCustomizersAndW3cTraceContext() throws Exception {
        TraceContextProvider traces = new TraceContextProvider() {
            @Override
            public String traceParent() {
                return "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
            }

            @Override
            public String traceState() {
                return "vendor=value";
            }
        };
        RestClient client = RestClientBuilder.builder()
                .apply(builder -> builder.addRequestInterceptor(
                        new UserAgentInterceptor("customized-client")))
                .addRequestInterceptor(new W3cTraceContextInterceptor(traces))
                .build();
        server.enqueue(new MockResponse().setBody("ok"));

        client.execute(request(), TypeRef.of(String.class));
        RecordedRequest recorded = server.takeRequest();

        assertThat(recorded.getHeader("User-Agent")).isEqualTo("customized-client");
        assertThat(recorded.getHeader("traceparent")).startsWith("00-4bf92f");
        assertThat(recorded.getHeader("tracestate")).isEqualTo("vendor=value");
    }

    @Test
    void bridgesSuccessfulCallsToANeutralMetricsCollector() {
        AtomicReference<Integer> status = new AtomicReference<Integer>();
        AtomicReference<URI> uri = new AtomicReference<URI>();
        MetricsObserver observer = new MetricsObserver((method, sanitizedUri, statusCode,
                                                         failureType, elapsedMillis) -> {
            status.set(statusCode);
            uri.set(sanitizedUri);
        });
        RestClient client = RestClientBuilder.builder().addObserver(observer).build();
        server.enqueue(new MockResponse().setBody("ok"));

        client.execute(request(), TypeRef.of(String.class));

        assertThat(status).hasValue(200);
        assertThat(uri.get().getPath()).isEqualTo("/integration");
    }

    private HttpRequest request() {
        return HttpRequest.builder().method(HttpMethod.GET)
                .uri(server.url("/integration").toString()).build();
    }
}
