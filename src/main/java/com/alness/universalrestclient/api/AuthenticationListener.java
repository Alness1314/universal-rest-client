package com.alness.universalrestclient.api;

/** Hook used as the foundation for application-controlled token renewal. */
public interface AuthenticationListener {
    void onUnauthorized(HttpRequest request, HttpResponse<?> response);
}
