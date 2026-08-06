package com.alness.universalrestclient.config.interceptor;

import com.alness.universalrestclient.api.AuthenticationListener;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.ResponseInterceptor;

import java.util.Objects;

/** Notifies application-controlled token renewal logic after a 401 response. */
public final class AuthenticationResponseInterceptor implements ResponseInterceptor {
    private final AuthenticationListener listener;

    public AuthenticationResponseInterceptor(AuthenticationListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener must not be null");
    }

    @Override
    public HttpResponse<?> intercept(HttpRequest request, HttpResponse<?> response) {
        if (response.statusCode() == 401) {
            listener.onUnauthorized(request, response);
        }
        return response;
    }
}
