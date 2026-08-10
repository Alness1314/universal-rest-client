package com.alness.universalrestclient.spring;

import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.testing.StubRestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class UniversalRestClientAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    UniversalRestClientAutoConfiguration.class));

    @Test
    void exposesDefaultClientForInjection() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(RestClient.class));
    }

    @Test
    void preservesApplicationProvidedClient() {
        StubRestClient supplied = new StubRestClient();

        contextRunner.withBean(RestClient.class, () -> supplied)
                .run(context -> assertThat(context.getBean(RestClient.class))
                        .isSameAs(supplied));
    }
}
