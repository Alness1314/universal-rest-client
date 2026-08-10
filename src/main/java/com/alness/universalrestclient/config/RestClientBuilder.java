package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.BodyCodec;
import com.alness.universalrestclient.api.ExchangeObserver;
import com.alness.universalrestclient.api.RequestInterceptor;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.ResponseInterceptor;
import com.alness.universalrestclient.api.RestClientCustomizer;
import com.alness.universalrestclient.internal.okhttp.OkHttpRestClient;
import com.alness.universalrestclient.internal.retry.RetryingRestClient;
import com.alness.universalrestclient.internal.async.ExecutorRestClient;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/** Public framework-neutral entry point for creating the default HTTP client. */
public final class RestClientBuilder {
    private RestClientConfig config = RestClientConfig.defaults();
    private BodyCodec bodyCodec = JacksonBodyCodec.withDefaults();
    private final List<RequestInterceptor> requestInterceptors =
            new ArrayList<RequestInterceptor>();
    private final List<ResponseInterceptor> responseInterceptors =
            new ArrayList<ResponseInterceptor>();
    private final List<ExchangeObserver> observers = new ArrayList<ExchangeObserver>();
    private RetryPolicy retryPolicy = RetryPolicy.disabled();
    private Executor executor = ForkJoinPool.commonPool();

    private RestClientBuilder() {
    }

    public static RestClientBuilder builder() {
        return new RestClientBuilder();
    }

    public RestClientBuilder config(RestClientConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        return this;
    }

    public RestClientBuilder bodyCodec(BodyCodec bodyCodec) {
        this.bodyCodec = Objects.requireNonNull(bodyCodec, "bodyCodec must not be null");
        return this;
    }

    public RestClientBuilder addRequestInterceptor(RequestInterceptor interceptor) {
        requestInterceptors.add(Objects.requireNonNull(interceptor,
                "interceptor must not be null"));
        return this;
    }

    public RestClientBuilder addResponseInterceptor(ResponseInterceptor interceptor) {
        responseInterceptors.add(Objects.requireNonNull(interceptor,
                "interceptor must not be null"));
        return this;
    }

    public RestClientBuilder addObserver(ExchangeObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer must not be null"));
        return this;
    }

    public RestClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        return this;
    }

    public RestClientBuilder executor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
        return this;
    }

    public RestClientBuilder apply(RestClientCustomizer customizer) {
        Objects.requireNonNull(customizer, "customizer must not be null").customize(this);
        return this;
    }

    public RestClient build() {
        OkHttpRestClient.Builder builder = OkHttpRestClient.builder()
                .config(config).bodyCodec(bodyCodec);
        for (RequestInterceptor interceptor : requestInterceptors) {
            builder.addRequestInterceptor(interceptor);
        }
        for (ResponseInterceptor interceptor : responseInterceptors) {
            builder.addResponseInterceptor(interceptor);
        }
        for (ExchangeObserver observer : observers) {
            builder.addObserver(observer);
        }
        RestClient client = builder.build();
        if (retryPolicy.maxAttempts() > 1) {
            client = new RetryingRestClient(client, retryPolicy);
        }
        return new ExecutorRestClient(client, executor);
    }
}
