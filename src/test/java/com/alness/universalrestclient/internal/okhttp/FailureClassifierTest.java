package com.alness.universalrestclient.internal.okhttp;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.exception.FailureType;
import com.alness.universalrestclient.exception.TransportException;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;

import static org.assertj.core.api.Assertions.assertThat;

class FailureClassifierTest {
    private final URI uri = URI.create("https://example.test/resource");

    @Test
    void classifiesNetworkFailuresWithoutInspectingMessagesInConsumerCode() {
        assertType(new UnknownHostException(), false, FailureType.DNS, true);
        assertType(new SSLException("handshake"), false, FailureType.TLS, false);
        assertType(new SocketTimeoutException("timeout"), false, FailureType.READ_TIMEOUT, true);
        assertType(new SocketTimeoutException("connect timed out"), false,
                FailureType.CONNECTION_TIMEOUT, true);
        assertType(new ConnectException(), false, FailureType.CONNECTION, true);
        assertType(new ConnectException(), true, FailureType.CANCELED, false);
    }

    private void assertType(java.io.IOException cause, boolean canceled,
                            FailureType type, boolean retryable) {
        TransportException exception = FailureClassifier.classify(cause, canceled,
                HttpMethod.GET, uri);

        assertThat(exception.failureType()).isEqualTo(type);
        assertThat(exception.method()).isEqualTo(HttpMethod.GET);
        assertThat(exception.uri()).isEqualTo(uri);
        assertThat(exception.isRetryable()).isEqualTo(retryable);
    }
}
