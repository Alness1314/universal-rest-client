package com.alness.universalrestclient.internal.retry;

import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RetryMode;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.RetryPolicy;
import com.alness.universalrestclient.exception.TransportException;
import com.alness.universalrestclient.testing.HttpResponses;
import com.alness.universalrestclient.testing.StubRestClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RetryingRestClientTest {
    private final RecordingSleeper sleeper = new RecordingSleeper();
    private final RetryPolicy policy = RetryPolicy.builder().maxAttempts(3)
            .initialDelayMillis(100).maxDelayMillis(1_000).jitterFactor(0).build();

    @Test
    void retriesSafeMethodsWithDeterministicExponentialBackoff() {
        StubRestClient delegate = new StubRestClient()
                .enqueueResponse(HttpResponses.withStatus(503, "first"))
                .enqueueResponse(HttpResponses.withStatus(500, "second"))
                .enqueueResponse(HttpResponses.successful("done"));
        RetryingRestClient client = client(delegate);

        HttpResponse<String> response = client.execute(request(HttpMethod.GET),
                TypeRef.of(String.class));

        assertThat(response.body()).isEqualTo("done");
        assertThat(delegate.recordedRequests()).hasSize(3);
        assertThat(sleeper.delays).containsExactly(100L, 200L);
    }

    @Test
    void honorsRetryAfterSecondsAndHttpDates() {
        long now = ZonedDateTime.parse("2026-08-06T12:00:00Z").toInstant().toEpochMilli();
        HttpResponse<String> seconds = response(429, "Retry-After", "2");
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(
                ZonedDateTime.parse("2026-08-06T12:00:03Z"));
        HttpResponse<String> dated = response(503, "Retry-After", date);
        StubRestClient delegate = new StubRestClient().enqueueResponse(seconds)
                .enqueueResponse(dated).enqueueResponse(HttpResponses.successful("done"));
        RetryingRestClient client = new RetryingRestClient(delegate, policy, sleeper, () -> now);

        client.execute(request(HttpMethod.GET), TypeRef.of(String.class));

        assertThat(sleeper.delays).containsExactly(2_000L, 3_000L);
    }

    @Test
    void neverRetriesPostUnlessExplicitlyMarkedIdempotent() {
        StubRestClient safeDelegate = new StubRestClient()
                .enqueueResponse(HttpResponses.withStatus(503, "stop"))
                .enqueueResponse(HttpResponses.successful("unused"));
        HttpResponse<String> stopped = client(safeDelegate).execute(request(HttpMethod.POST),
                TypeRef.of(String.class));

        StubRestClient enabledDelegate = new StubRestClient()
                .enqueueResponse(HttpResponses.withStatus(503, "retry"))
                .enqueueResponse(HttpResponses.successful("done"));
        HttpRequest enabled = request(HttpMethod.POST).toBuilder()
                .retryMode(RetryMode.ENABLED).build();
        HttpResponse<String> retried = client(enabledDelegate).execute(enabled,
                TypeRef.of(String.class));

        assertThat(stopped.statusCode()).isEqualTo(503);
        assertThat(safeDelegate.recordedRequests()).hasSize(1);
        assertThat(retried.body()).isEqualTo("done");
        assertThat(enabledDelegate.recordedRequests()).hasSize(2);
    }

    @Test
    void retriesOnlyRecoverableTransportFailures() {
        StubRestClient delegate = new StubRestClient()
                .enqueueFailure(new TransportException("offline", null, true))
                .enqueueResponse(HttpResponses.successful("done"));

        HttpResponse<String> response = client(delegate).execute(request(HttpMethod.GET),
                TypeRef.of(String.class));

        assertThat(response.body()).isEqualTo("done");
        assertThat(sleeper.delays).containsExactly(100L);
    }

    @Test
    void doesNotRetryOneShotBodies() {
        StubRestClient delegate = new StubRestClient()
                .enqueueResponse(HttpResponses.withStatus(503, "stop"))
                .enqueueResponse(HttpResponses.successful("unused"));
        HttpRequest request = request(HttpMethod.PUT).toBuilder().body(new OneShotBody()).build();

        HttpResponse<String> response = client(delegate).execute(request, TypeRef.of(String.class));

        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(delegate.recordedRequests()).hasSize(1);
    }

    private RetryingRestClient client(StubRestClient delegate) {
        return new RetryingRestClient(delegate, policy, sleeper, () -> 0L);
    }

    private static HttpRequest request(HttpMethod method) {
        return HttpRequest.builder().method(method).uri("https://example.test/retry").build();
    }

    private static HttpResponse<String> response(int status, String header, String value) {
        return HttpResponse.<String>builder().statusCode(status)
                .headers(HttpHeaders.builder().set(header, value).build()).body("retry").build();
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Long> delays = new ArrayList<Long>();

        @Override
        public void sleep(long millis) {
            delays.add(millis);
        }
    }

    private static final class OneShotBody implements HttpBody {
        @Override
        public String contentType() {
            return "application/octet-stream";
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public void writeTo(OutputStream output) throws IOException {
            output.write(1);
        }
    }
}
