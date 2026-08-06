package com.alness.universalrestclient.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpResponseTest {
    @Test
    void classifiesSuccessfulStatusesAndAllowsEmptyBodies() {
        HttpResponse<Void> response = HttpResponse.<Void>builder().statusCode(204).build();

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNull();
        assertThat(response.rawBody()).isEmpty();
    }

    @Test
    void validatesTheStatusCode() {
        assertThatThrownBy(() -> HttpResponse.builder().statusCode(99).build())
                .isInstanceOf(IllegalStateException.class);
    }
}
