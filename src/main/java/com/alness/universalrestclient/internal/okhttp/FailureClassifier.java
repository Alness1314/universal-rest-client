package com.alness.universalrestclient.internal.okhttp;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.exception.FailureType;
import com.alness.universalrestclient.exception.TransportException;
import com.alness.universalrestclient.internal.SensitiveDataSanitizer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;

/** Maps transport-specific failures to stable public categories. */
final class FailureClassifier {
    private FailureClassifier() {
    }

    static TransportException classify(IOException exception, boolean canceled,
                                       HttpMethod method, URI uri) {
        FailureType type;
        boolean retryable;
        String message;
        if (canceled) {
            type = FailureType.CANCELED;
            retryable = false;
            message = "HTTP call was canceled";
        } else if (exception instanceof UnknownHostException) {
            type = FailureType.DNS;
            retryable = true;
            message = "HTTP DNS resolution failed";
        } else if (exception instanceof SSLException) {
            type = FailureType.TLS;
            retryable = false;
            message = "HTTP TLS negotiation failed";
        } else if (exception instanceof SocketTimeoutException
                || exception instanceof InterruptedIOException) {
            boolean connect = exception.getMessage() != null
                    && exception.getMessage().toLowerCase(java.util.Locale.ROOT).contains("connect");
            type = connect ? FailureType.CONNECTION_TIMEOUT : FailureType.READ_TIMEOUT;
            retryable = true;
            message = connect ? "HTTP connection timed out" : "HTTP read timed out";
        } else if (exception instanceof ConnectException) {
            type = FailureType.CONNECTION;
            retryable = true;
            message = "HTTP connection failed";
        } else {
            type = FailureType.CONNECTION;
            retryable = true;
            message = "HTTP transport failed";
        }
        return new TransportException(message, exception, type, method,
                SensitiveDataSanitizer.uri(uri), retryable);
    }
}
