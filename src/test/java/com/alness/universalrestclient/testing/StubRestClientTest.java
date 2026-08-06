package com.alness.universalrestclient.testing;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.TransportException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StubRestClientTest {
    private final HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .uri("https://example.test/items")
            .build();

    @Test
    void returnsQueuedResponsesAndRecordsRequests() {
        StubRestClient client = new StubRestClient()
                .enqueueResponse(HttpResponses.successful("ok"));

        HttpResponse<String> response = client.execute(request, TypeRef.of(String.class));

        assertThat(response.body()).isEqualTo("ok");
        assertThat(client.recordedRequests()).containsExactly(request);
        assertThat(client.pendingOutcomes()).isZero();
    }

    @Test
    void throwsQueuedFailures() {
        TransportException failure = new TransportException("offline", null, true);
        StubRestClient client = new StubRestClient().enqueueFailure(failure);

        assertThatThrownBy(() -> client.execute(request, TypeRef.of(String.class)))
                .isSameAs(failure);
    }

    @Test
    void supportsCancelingAStubCallBeforeExecution() {
        StubRestClient client = new StubRestClient()
                .enqueueResponse(HttpResponses.successful("unused"));
        RestCall<String> call = client.newCall(request, TypeRef.of(String.class));

        call.cancel();

        assertThatThrownBy(call::execute)
                .isInstanceOf(TransportException.class)
                .hasMessage("Call was canceled");
        assertThat(call.isCanceled()).isTrue();
        assertThat(call.isExecuted()).isTrue();
    }
}
