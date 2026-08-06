package com.alness.universalrestclient.config;

import com.alness.universalrestclient.api.RestClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientBuilderTest {
    @Test
    void createsTheDefaultReusableClientThroughThePublicApi() {
        RestClient client = RestClientBuilder.builder()
                .config(RestClientConfig.defaults())
                .build();

        assertThat(client).isNotNull();
    }
}
