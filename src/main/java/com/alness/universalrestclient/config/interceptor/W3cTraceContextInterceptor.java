package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;
import com.alness.universalrestclient.api.TraceContextProvider;

import java.util.Objects;

/** Propagates W3C traceparent and tracestate values supplied by a tracing backend. */
public final class W3cTraceContextInterceptor implements RequestInterceptor {
    private final TraceContextProvider provider;

    public W3cTraceContextInterceptor(TraceContextProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        HttpHeaders.Builder headers = request.headers().toBuilder();
        String traceParent = provider.traceParent();
        String traceState = provider.traceState();
        if (traceParent != null && !traceParent.isEmpty()) {
            headers.set("traceparent", traceParent);
        }
        if (traceState != null && !traceState.isEmpty()) {
            headers.set("tracestate", traceState);
        }
        return request.toBuilder().headers(headers.build()).build();
    }
}
