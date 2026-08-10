package com.alness.universalrestclient.spring;

import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.RestClientCustomizer;
import com.alness.universalrestclient.config.RestClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Optional Spring Boot integration that exposes one customizable client bean. */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
public class UniversalRestClientAutoConfiguration {
    /**
     * Creates the default client unless the application already provides one.
     *
     * @param customizers optional application customizers
     * @return configured REST client
     */
    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    public RestClient universalRestClient(
            ObjectProvider<RestClientCustomizer> customizers) {
        RestClientBuilder builder = RestClientBuilder.builder();
        customizers.orderedStream().forEach(builder::apply);
        return builder.build();
    }
}
