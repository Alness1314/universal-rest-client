package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;

import java.util.Objects;

/** Applies a configured User-Agent while preserving an explicit request value. */
public final class UserAgentInterceptor implements RequestInterceptor {
    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = Objects.requireNonNull(userAgent, "userAgent must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (request.headers().contains("User-Agent")) {
            return request;
        }
        HttpHeaders headers = request.headers().toBuilder().set("User-Agent", userAgent).build();
        return request.toBuilder().headers(headers).build();
    }
}
