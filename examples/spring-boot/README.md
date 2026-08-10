# Spring Boot example

Declare `com.alness:universal-rest-client:1.1.0`. The library automatically
exposes a `RestClient` bean, so services only inject it:

```java
@Service
class PokemonService {
    private final RestClient restClient;

    PokemonService(RestClient restClient) {
        this.restClient = restClient;
    }
}
```

Use a `RestClientCustomizer` bean for shared timeouts, interceptors or retries.
Declaring an application-owned `RestClient` bean disables the default one.
