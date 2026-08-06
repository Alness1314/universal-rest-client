package com.alness.universalrestclient.testing;

import com.alness.universalrestclient.api.HttpResponse;

/** Convenient response factories for consumer tests. */
public final class HttpResponses {
    private HttpResponses() {
    }

    public static <T> HttpResponse<T> successful(T body) {
        return HttpResponse.<T>builder().statusCode(200).body(body).build();
    }

    public static <T> HttpResponse<T> withStatus(int statusCode, T body) {
        return HttpResponse.<T>builder().statusCode(statusCode).body(body).build();
    }
}
