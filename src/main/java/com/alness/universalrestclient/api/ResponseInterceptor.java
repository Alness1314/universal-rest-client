package com.alness.universalrestclient.api;

/** Observes or transforms a successful transport response. */
public interface ResponseInterceptor {
    HttpResponse<?> intercept(HttpRequest request, HttpResponse<?> response);
}
