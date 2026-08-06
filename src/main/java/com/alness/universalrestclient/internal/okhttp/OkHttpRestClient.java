package com.alness.universalrestclient.internal.okhttp;

import com.alness.universalrestclient.api.BodyCodec;
import com.alness.universalrestclient.api.ExchangeObserver;
import com.alness.universalrestclient.api.HttpBody;
import com.alness.universalrestclient.api.HttpHeaders;
import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestCall;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.RequestInterceptor;
import com.alness.universalrestclient.api.ResponseInterceptor;
import com.alness.universalrestclient.api.TypeRef;
import com.alness.universalrestclient.config.HttpErrorPolicy;
import com.alness.universalrestclient.config.RestClientConfig;
import com.alness.universalrestclient.exception.FailureType;
import com.alness.universalrestclient.exception.HttpStatusException;
import com.alness.universalrestclient.exception.InvalidRequestException;
import com.alness.universalrestclient.exception.ResponseTooLargeException;
import com.alness.universalrestclient.exception.SerializationException;
import com.alness.universalrestclient.exception.TransportException;
import com.alness.universalrestclient.internal.RawBodyCodec;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/** Thread-safe REST client backed by a reusable OkHttp client and connection pool. */
public final class OkHttpRestClient implements RestClient {
    private static final byte[] EMPTY_BODY = new byte[0];
    private final OkHttpClient client;
    private final RestClientConfig config;
    private final BodyCodec bodyCodec;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseInterceptor> responseInterceptors;
    private final List<ExchangeObserver> observers;

    private OkHttpRestClient(Builder builder) {
        this.config = builder.config;
        this.bodyCodec = builder.bodyCodec;
        this.requestInterceptors = immutableCopy(builder.requestInterceptors);
        this.responseInterceptors = immutableCopy(builder.responseInterceptors);
        this.observers = immutableCopy(builder.observers);
        OkHttpClient.Builder clientBuilder = builder.baseClient == null
                ? new OkHttpClient.Builder() : builder.baseClient.newBuilder();
        clientBuilder
                .connectTimeout(config.connectTimeoutMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeoutMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeoutMillis(), TimeUnit.MILLISECONDS)
                .followRedirects(config.followRedirects())
                .followSslRedirects(config.followRedirects());
        if (builder.connectionPool != null) {
            clientBuilder.connectionPool(builder.connectionPool);
        }
        this.client = clientBuilder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public OkHttpClient okHttpClient() {
        return client;
    }

    @Override
    public <T> RestCall<T> newCall(HttpRequest request, TypeRef<T> responseType) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");
        HttpRequest effectiveRequest = request;
        for (RequestInterceptor interceptor : requestInterceptors) {
            effectiveRequest = Objects.requireNonNull(interceptor.intercept(effectiveRequest),
                    "request interceptor returned null");
        }
        Request nativeRequest = toNativeRequest(effectiveRequest);
        return new OkHttpRestCall<T>(client.newCall(nativeRequest), responseType,
                config.maxResponseBytes(), bodyCodec, effectiveRequest, nativeRequest.url().uri(),
                config.errorPolicy(), responseInterceptors, observers);
    }

    private Request toNativeRequest(HttpRequest request) {
        HttpUrl.Builder url;
        try {
            url = HttpUrl.get(request.uri().toURL()).newBuilder();
        } catch (MalformedURLException | IllegalArgumentException exception) {
            throw new InvalidRequestException("Invalid request URI", exception,
                    FailureType.INVALID_URL, request.method(),
                    SensitiveDataSanitizer.uri(request.uri()));
        }
        for (Map.Entry<String, List<String>> parameter : request.queryParameters().entrySet()) {
            for (String value : parameter.getValue()) {
                url.addQueryParameter(parameter.getKey(), value);
            }
        }

        Request.Builder nativeRequest = new Request.Builder().url(url.build());
        for (Map.Entry<String, List<String>> header : request.headers().asMap().entrySet()) {
            for (String value : header.getValue()) {
                nativeRequest.addHeader(header.getKey(), value);
            }
        }

        HttpBody body = request.hasTypedBody()
                ? bodyCodec.serialize(request.bodyValue(), request.bodyType()) : request.body();
        String method = request.method().name();
        if (("GET".equals(method) || "HEAD".equals(method)) && body != null) {
            throw new InvalidRequestException(method + " requests cannot have a body");
        }
        RequestBody nativeBody = body == null ? null : new BodyAdapter(body);
        if (nativeBody == null && requiresBody(method)) {
            nativeBody = new ByteArrayRequestBody(EMPTY_BODY, null);
        }
        try {
            return nativeRequest.method(method, nativeBody).build();
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Invalid " + method + " request", exception);
        }
    }

    private static boolean requiresBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    /** Builder that creates independent clients while allowing explicit pool sharing. */
    public static final class Builder {
        private RestClientConfig config = RestClientConfig.defaults();
        private OkHttpClient baseClient;
        private ConnectionPool connectionPool;
        private BodyCodec bodyCodec = new RawBodyCodec();
        private final List<RequestInterceptor> requestInterceptors =
                new ArrayList<RequestInterceptor>();
        private final List<ResponseInterceptor> responseInterceptors =
                new ArrayList<ResponseInterceptor>();
        private final List<ExchangeObserver> observers = new ArrayList<ExchangeObserver>();

        public Builder config(RestClientConfig config) {
            this.config = Objects.requireNonNull(config, "config must not be null");
            return this;
        }

        public Builder baseClient(OkHttpClient client) {
            this.baseClient = Objects.requireNonNull(client, "client must not be null");
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = Objects.requireNonNull(connectionPool,
                    "connectionPool must not be null");
            return this;
        }

        public Builder bodyCodec(BodyCodec bodyCodec) {
            this.bodyCodec = Objects.requireNonNull(bodyCodec, "bodyCodec must not be null");
            return this;
        }

        public Builder addRequestInterceptor(RequestInterceptor interceptor) {
            requestInterceptors.add(Objects.requireNonNull(interceptor,
                    "interceptor must not be null"));
            return this;
        }

        public Builder addResponseInterceptor(ResponseInterceptor interceptor) {
            responseInterceptors.add(Objects.requireNonNull(interceptor,
                    "interceptor must not be null"));
            return this;
        }

        public Builder addObserver(ExchangeObserver observer) {
            observers.add(Objects.requireNonNull(observer, "observer must not be null"));
            return this;
        }

        public OkHttpRestClient build() {
            return new OkHttpRestClient(this);
        }
    }

    private static final class OkHttpRestCall<T> implements RestCall<T> {
        private final okhttp3.Call call;
        private final TypeRef<T> responseType;
        private final long maxResponseBytes;
        private final BodyCodec bodyCodec;
        private final HttpRequest request;
        private final java.net.URI uri;
        private final HttpErrorPolicy errorPolicy;
        private final List<ResponseInterceptor> responseInterceptors;
        private final List<ExchangeObserver> observers;

        private OkHttpRestCall(okhttp3.Call call, TypeRef<T> responseType, long maxResponseBytes,
                               BodyCodec bodyCodec, HttpRequest request, java.net.URI uri,
                               HttpErrorPolicy errorPolicy,
                               List<ResponseInterceptor> responseInterceptors,
                               List<ExchangeObserver> observers) {
            this.call = call;
            this.responseType = responseType;
            this.maxResponseBytes = maxResponseBytes;
            this.bodyCodec = bodyCodec;
            this.request = request;
            this.uri = uri;
            this.errorPolicy = errorPolicy;
            this.responseInterceptors = responseInterceptors;
            this.observers = observers;
        }

        @Override
        public HttpResponse<T> execute() {
            long started = System.nanoTime();
            notifyRequest(observers, request);
            try (Response response = call.execute()) {
                ResponseBody body = response.body();
                byte[] bytes = body == null ? EMPTY_BODY : readBody(body, maxResponseBytes);
                HttpHeaders headers = toHeaders(response);
                if (errorPolicy.shouldThrow(response.code())) {
                    throw observed(new HttpStatusException(request.method(),
                            SensitiveDataSanitizer.uri(uri),
                            response.code(), SensitiveDataSanitizer.headers(headers), bytes,
                            retryableStatus(response.code())), started);
                }
                String contentType = body == null || body.contentType() == null
                        ? null : body.contentType().toString();
                T decoded;
                try {
                    decoded = bodyCodec.deserialize(bytes, responseType, contentType);
                } catch (SerializationException exception) {
                    throw observed(new SerializationException(exception.getMessage(),
                            exception.getCause(), request.method(), SensitiveDataSanitizer.uri(uri),
                            response.code(), SensitiveDataSanitizer.headers(headers), bytes), started);
                }
                HttpResponse<T> result = HttpResponse.<T>builder()
                        .statusCode(response.code())
                        .headers(headers)
                        .rawBody(bytes)
                        .body(decoded)
                        .build();
                result = applyResponseInterceptors(request, result, responseInterceptors);
                notifyResponse(observers, request, result, elapsedMillis(started));
                return result;
            } catch (ResponseLimitIOException exception) {
                throw observed(new ResponseTooLargeException(request.method(),
                        SensitiveDataSanitizer.uri(uri), maxResponseBytes, exception), started);
            } catch (IOException exception) {
                throw observed(FailureClassifier.classify(exception, call.isCanceled(),
                        request.method(), uri), started);
            }
        }

        @Override
        public void cancel() {
            call.cancel();
        }

        @Override
        public boolean isCanceled() {
            return call.isCanceled();
        }

        @Override
        public boolean isExecuted() {
            return call.isExecuted();
        }

        private <E extends com.alness.universalrestclient.exception.RestClientException> E observed(
                E failure, long started) {
            notifyFailure(observers, request, failure, elapsedMillis(started));
            return failure;
        }
    }

    private static HttpHeaders toHeaders(Response response) {
        HttpHeaders.Builder builder = HttpHeaders.builder();
        for (int index = 0; index < response.headers().size(); index++) {
            builder.add(response.headers().name(index), response.headers().value(index));
        }
        return builder.build();
    }

    private static byte[] readBody(ResponseBody body, long maximum) throws IOException {
        long declaredLength = body.contentLength();
        if (declaredLength > maximum) {
            throw new ResponseLimitIOException();
        }
        try (InputStream input = body.byteStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8_192];
            long total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > maximum) {
                    throw new ResponseLimitIOException();
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static boolean retryableStatus(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }

    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> applyResponseInterceptors(HttpRequest request,
            HttpResponse<T> response, List<ResponseInterceptor> interceptors) {
        HttpResponse<?> current = response;
        for (ResponseInterceptor interceptor : interceptors) {
            current = Objects.requireNonNull(interceptor.intercept(request, current),
                    "response interceptor returned null");
        }
        return (HttpResponse<T>) current;
    }

    private static void notifyRequest(List<ExchangeObserver> observers, HttpRequest request) {
        for (ExchangeObserver observer : observers) {
            try {
                observer.onRequest(request);
            } catch (RuntimeException ignored) {
                // Observability must not break an HTTP exchange.
            }
        }
    }

    private static void notifyResponse(List<ExchangeObserver> observers, HttpRequest request,
                                       HttpResponse<?> response, long elapsedMillis) {
        for (ExchangeObserver observer : observers) {
            try {
                observer.onResponse(request, response, elapsedMillis);
            } catch (RuntimeException ignored) {
                // Observability must not break an HTTP exchange.
            }
        }
    }

    private static void notifyFailure(List<ExchangeObserver> observers, HttpRequest request,
                                      com.alness.universalrestclient.exception.RestClientException failure,
                                      long elapsedMillis) {
        for (ExchangeObserver observer : observers) {
            try {
                observer.onFailure(request, failure, elapsedMillis);
            } catch (RuntimeException ignored) {
                // Observability must not replace the original failure.
            }
        }
    }

    private static long elapsedMillis(long started) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }

    private static final class ResponseLimitIOException extends IOException {
        private static final long serialVersionUID = 1L;
    }

    private static final class BodyAdapter extends RequestBody {
        private final HttpBody body;
        private final MediaType mediaType;

        private BodyAdapter(HttpBody body) {
            this.body = body;
            this.mediaType = body.contentType() == null ? null : MediaType.parse(body.contentType());
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }

        @Override
        public long contentLength() {
            return body.contentLength();
        }

        @Override
        public boolean isOneShot() {
            return !body.isRepeatable();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            body.writeTo(sink.outputStream());
        }
    }

    private static final class ByteArrayRequestBody extends RequestBody {
        private final byte[] bytes;
        private final MediaType mediaType;

        private ByteArrayRequestBody(byte[] bytes, MediaType mediaType) {
            this.bytes = bytes;
            this.mediaType = mediaType;
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }

        @Override
        public long contentLength() {
            return bytes.length;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(bytes);
        }
    }
}
