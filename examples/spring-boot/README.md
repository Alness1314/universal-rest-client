# Spring Boot example

Declare `com.alness:universal-rest-client:1.0.0` and expose the framework-neutral
client as a regular bean:

```java
@Configuration
class HttpClientConfiguration {
    @Bean
    RestClient restClient(ObjectMapper objectMapper) {
        return RestClientBuilder.builder()
                .bodyCodec(new JacksonBodyCodec(objectMapper))
                .build();
    }
}
```

The library does not require component scanning, bean overriding or Spring Cloud.
