package com.alness.universalrestclient.api;

/** Supplies the current authentication token at request execution time. */
public interface TokenProvider {
    String token();
}
