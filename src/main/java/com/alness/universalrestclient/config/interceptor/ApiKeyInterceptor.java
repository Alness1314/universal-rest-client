package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;

import java.util.Objects;
import java.util.function.Supplier;

/** Adds a dynamically supplied API key to a configurable header. */
public final class ApiKeyInterceptor implements RequestInterceptor {
    private final String headerName;
    private final Supplier<String> valueProvider;

    public ApiKeyInterceptor(String headerName, Supplier<String> valueProvider) {
        this.headerName = Objects.requireNonNull(headerName, "headerName must not be null");
        this.valueProvider = Objects.requireNonNull(valueProvider,
                "valueProvider must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        String value = valueProvider.get();
        if (value == null || value.isEmpty()) {
            return request;
        }
        HttpHeaders headers = request.headers().toBuilder().set(headerName, value).build();
        return request.toBuilder().headers(headers).build();
    }
}
