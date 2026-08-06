package com.alness.universalrestclient.api;

/** Supplies W3C trace context from OpenTelemetry or another tracing system. */
public interface TraceContextProvider {
    String traceParent();

    String traceState();
}
