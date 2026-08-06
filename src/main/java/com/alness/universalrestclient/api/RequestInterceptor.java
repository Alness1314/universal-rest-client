package com.alness.universalrestclient.api;

/** Transforms a request before it is converted by the HTTP transport. */
public interface RequestInterceptor {
    HttpRequest intercept(HttpRequest request);
}
