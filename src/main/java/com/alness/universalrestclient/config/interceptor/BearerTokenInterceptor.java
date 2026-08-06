package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;
import com.alness.universalrestclient.api.TokenProvider;

import java.util.Objects;

/** Adds the latest bearer token without storing it in the client configuration. */
public final class BearerTokenInterceptor implements RequestInterceptor {
    private final TokenProvider tokenProvider;

    public BearerTokenInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        String token = tokenProvider.token();
        if (token == null || token.trim().isEmpty()) {
            return request;
        }
        String value = token.startsWith("Bearer ") ? token : "Bearer " + token;
        HttpHeaders headers = request.headers().toBuilder().set("Authorization", value).build();
        return request.toBuilder().headers(headers).build();
    }
}
