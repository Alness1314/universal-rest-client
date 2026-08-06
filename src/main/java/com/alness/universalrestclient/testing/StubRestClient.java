package com.alness.universalrestclient.testing;

import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.exception.RestClientException;
import com.alness.universalrestclient.exception.TransportException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Scriptable in-memory client for consumer unit tests. */
public final class StubRestClient implements RestClient {
    private final Deque<Outcome> outcomes = new ArrayDeque<Outcome>();
    private final List<HttpRequest> recordedRequests = new ArrayList<HttpRequest>();

    @Override
    public <T> RestCall<T> newCall(final HttpRequest request, final TypeRef<T> responseType) {
        return new StubRestCall<T>(this, request, responseType);
    }

    public synchronized StubRestClient enqueueResponse(HttpResponse<?> response) {
        outcomes.addLast(Outcome.response(Objects.requireNonNull(response, "response must not be null")));
        return this;
    }

    public synchronized StubRestClient enqueueFailure(RestClientException failure) {
        outcomes.addLast(Outcome.failure(Objects.requireNonNull(failure, "failure must not be null")));
        return this;
    }

    @Override
    public synchronized <T> HttpResponse<T> execute(HttpRequest request, TypeRef<T> responseType) {
        recordedRequests.add(Objects.requireNonNull(request, "request must not be null"));
        Objects.requireNonNull(responseType, "responseType must not be null");
        Outcome outcome = outcomes.pollFirst();
        if (outcome == null) {
            throw new IllegalStateException("No stub outcome is available");
        }
        if (outcome.failure != null) {
            throw outcome.failure;
        }
        return cast(outcome.response);
    }

    public synchronized List<HttpRequest> recordedRequests() {
        return Collections.unmodifiableList(new ArrayList<HttpRequest>(recordedRequests));
    }

    public synchronized int pendingOutcomes() {
        return outcomes.size();
    }

    public synchronized void reset() {
        outcomes.clear();
        recordedRequests.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> cast(HttpResponse<?> response) {
        return (HttpResponse<T>) response;
    }

    private static final class Outcome {
        private final HttpResponse<?> response;
        private final RestClientException failure;

        private Outcome(HttpResponse<?> response, RestClientException failure) {
            this.response = response;
            this.failure = failure;
        }

        private static Outcome response(HttpResponse<?> response) {
            return new Outcome(response, null);
        }

        private static Outcome failure(RestClientException failure) {
            return new Outcome(null, failure);
        }
    }

    private static final class StubRestCall<T> implements RestCall<T> {
        private final StubRestClient client;
        private final HttpRequest request;
        private final TypeRef<T> responseType;
        private boolean canceled;
        private boolean executed;

        private StubRestCall(StubRestClient client, HttpRequest request, TypeRef<T> responseType) {
            this.client = client;
            this.request = request;
            this.responseType = responseType;
        }

        @Override
        public synchronized HttpResponse<T> execute() {
            if (executed) {
                throw new IllegalStateException("A call can only be executed once");
            }
            executed = true;
            if (canceled) {
                throw new TransportException("Call was canceled", null, false);
            }
            return client.execute(request, responseType);
        }

        @Override
        public synchronized void cancel() {
            canceled = true;
        }

        @Override
        public synchronized boolean isCanceled() {
            return canceled;
        }

        @Override
        public synchronized boolean isExecuted() {
            return executed;
        }
    }
}
