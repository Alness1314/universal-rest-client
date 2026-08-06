package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;

import java.util.Objects;
import java.util.function.Supplier;

/** Adds a correlation identifier only when the request does not already have one. */
public final class CorrelationIdInterceptor implements RequestInterceptor {
    public static final String DEFAULT_HEADER = "X-Correlation-ID";
    private final String headerName;
    private final Supplier<String> valueProvider;

    public CorrelationIdInterceptor() {
        this(DEFAULT_HEADER, () -> java.util.UUID.randomUUID().toString());
    }

    public CorrelationIdInterceptor(String headerName, Supplier<String> valueProvider) {
        this.headerName = Objects.requireNonNull(headerName, "headerName must not be null");
        this.valueProvider = Objects.requireNonNull(valueProvider,
                "valueProvider must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (request.headers().contains(headerName)) {
            return request;
        }
        HttpHeaders headers = request.headers().toBuilder()
                .set(headerName, valueProvider.get()).build();
        return request.toBuilder().headers(headers).build();
    }
}
