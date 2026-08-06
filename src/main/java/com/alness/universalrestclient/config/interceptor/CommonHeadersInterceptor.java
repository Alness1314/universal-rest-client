package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.RequestInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Applies reusable headers while allowing a request to override them. */
public final class CommonHeadersInterceptor implements RequestInterceptor {
    private final HttpHeaders commonHeaders;

    public CommonHeadersInterceptor(HttpHeaders commonHeaders) {
        this.commonHeaders = Objects.requireNonNull(commonHeaders,
                "commonHeaders must not be null");
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        HttpHeaders.Builder headers = commonHeaders.toBuilder();
        for (Map.Entry<String, List<String>> entry : request.headers().asMap().entrySet()) {
            headers.remove(entry.getKey());
            for (String value : entry.getValue()) {
                headers.add(entry.getKey(), value);
            }
        }
        return request.toBuilder().headers(headers.build()).build();
    }
}
