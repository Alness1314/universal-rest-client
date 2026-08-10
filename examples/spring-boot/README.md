# Ejemplo Spring Boot

Spring Boot crea automáticamente un bean `RestClient` al detectar la librería.
No se utiliza `@Module`, `@InstallIn`, `@Provides` ni `@Singleton`; esas
anotaciones pertenecen a Hilt en Android.

## Dependencia

```xml
<dependency>
    <groupId>com.alness</groupId>
    <artifactId>universal-rest-client</artifactId>
    <version>1.1.1</version>
</dependency>
```

## Configuración compartida

Coloque la configuración dentro de un paquete escaneado por Spring, por ejemplo:

```text
src/main/java/mx/alness/testingapi/config/RestClientConfiguration.java
```

```java
package mx.alness.testingapi.config;

import com.alness.universalrestclient.api.RestClientCustomizer;
import com.alness.universalrestclient.config.HttpErrorPolicy;
import com.alness.universalrestclient.config.RestClientConfig;
import com.alness.universalrestclient.config.RetryPolicy;
import com.alness.universalrestclient.config.interceptor.UserAgentInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfiguration {

    @Bean
    RestClientCustomizer applicationRestClientDefaults() {
        return builder -> builder
                .config(RestClientConfig.builder()
                        .connectTimeoutMillis(5_000)
                        .readTimeoutMillis(10_000)
                        .errorPolicy(HttpErrorPolicy.THROW_ON_4XX_5XX)
                        .build())
                .retryPolicy(RetryPolicy.builder()
                        .maxAttempts(3)
                        .initialDelayMillis(100)
                        .maxDelayMillis(2_000)
                        .build())
                .addRequestInterceptor(
                        new UserAgentInterceptor("testingapi/1.0"));
    }
}
```

El customizer se aplica antes de crear el bean automático. Si no necesita
configuración especial, puede omitir completamente esta clase.

## Inyección en el servicio

```java
@Service
public class PokemonService {
    private final RestClient restClient;

    public PokemonService(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokemonResponse findByNumber(int number) {
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .uri("https://pokeapi.co/api/v2/pokemon/" + number)
                .build();

        return restClient.execute(
                request,
                TypeRef.of(PokemonResponse.class)
        ).body();
    }
}
```

No construya el cliente dentro del servicio ni utilice `@PostConstruct`. El bean
es singleton y debe compartirse para reutilizar conexiones.
