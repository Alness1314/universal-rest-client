# Universal REST Client

Cliente HTTP reutilizable para Java, Android y Spring Boot. Usa OkHttp como
transporte y Jackson para procesar JSON de forma predeterminada.

## Características

- Java 8 y Android API 21+.
- Cliente reutilizable y thread-safe.
- JSON listo para usar, incluyendo fechas y tipos genéricos.
- Errores estructurados, interceptores, autenticación y logging seguro.
- Reintentos con backoff, jitter y soporte para `Retry-After`.
- API síncrona, `CompletableFuture` cancelable y extensión Kotlin `suspend`.
- Autoconfiguración para inyección directa en Spring Boot.

## Dependencia Maven

GitHub Packages debe declararse en el `pom.xml` del proyecto consumidor:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Alness1314/universal-rest-client</url>
    </repository>
</repositories>
```

Después agrega la dependencia:

```xml
<dependency>
    <groupId>com.alness</groupId>
    <artifactId>universal-rest-client</artifactId>
    <version>1.1.1</version>
</dependency>
```

Las credenciales de GitHub Packages deben colocarse en el archivo global de
Maven, no en el repositorio del proyecto:

```text
C:\Users\TU_USUARIO\.m2\settings.xml
```

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_TOKEN_CON_READ_PACKAGES</password>
        </server>
    </servers>
</settings>
```

## Ejemplo completo con Spring Boot

Spring Boot detecta la librería y crea automáticamente un bean `RestClient`.
No es necesario construir el cliente en el servicio, usar `@PostConstruct` ni
registrar `JacksonBodyCodec`.

Una organización sugerida es:

```text
src/main/java/mx/alness/testingapi
├── config
│   └── RestClientConfiguration.java   (opcional)
├── controller
│   └── PokeController.java
├── dto
│   └── PokemonResponse.java
└── service
    └── impl
        └── PokeServiceImpl.java
```

### 1. DTO de respuesta

Archivo sugerido:

```text
src/main/java/mx/alness/testingapi/dto/PokemonResponse.java
```

```java
package mx.alness.testingapi.dto;

import java.util.List;

public class PokemonResponse {
    private Integer id;
    private String name;
    private List<PokemonType> types;

    // Getters y setters

    public static class PokemonType {
        private TypeInfo type;
        // Getters y setters
    }

    public static class TypeInfo {
        private String name;
        // Getters y setters
    }
}
```

El DTO puede contener únicamente los campos que necesita la aplicación. Los
campos adicionales enviados por la API remota se ignoran de forma
predeterminada.

### 2. Servicio con el cliente inyectado

Archivo sugerido:

```text
src/main/java/mx/alness/testingapi/service/impl/PokeServiceImpl.java
```

```java
package mx.alness.testingapi.service.impl;

import com.alness.universalrestclient.api.HttpMethod;
import com.alness.universalrestclient.api.HttpRequest;
import com.alness.universalrestclient.api.HttpResponse;
import com.alness.universalrestclient.api.RestClient;
import com.alness.universalrestclient.api.TypeRef;
import mx.alness.testingapi.dto.PokemonResponse;
import org.springframework.stereotype.Service;

@Service
public class PokeServiceImpl {
    private final RestClient restClient;

    public PokeServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokemonResponse getPokemon(String name) {
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .uri("https://pokeapi.co/api/v2/pokemon/" + name)
                .build();

        HttpResponse<PokemonResponse> response = restClient.execute(
                request,
                TypeRef.of(PokemonResponse.class)
        );

        return response.body();
    }
}
```

### 3. Controlador

```java
@RestController
@RequestMapping("/api/pokemon")
public class PokeController {
    private final PokeServiceImpl pokeService;

    public PokeController(PokeServiceImpl pokeService) {
        this.pokeService = pokeService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<PokemonResponse> getPokemon(
            @PathVariable String name) {
        return ResponseEntity.ok(pokeService.getPokemon(name));
    }
}
```

Con esta configuración, una llamada a:

```text
GET http://localhost:8080/api/pokemon/pikachu
```

devuelve un objeto como:

```json
{
  "id": 25,
  "name": "pikachu",
  "types": [
    {
      "type": {
        "name": "electric"
      }
    }
  ]
}
```

## Configuración opcional en Spring Boot

La clase de configuración solo es necesaria cuando se quieren cambiar
timeouts, reintentos, headers o interceptores. Debe colocarse dentro de un
paquete incluido en el escaneo de Spring, por ejemplo:

```text
src/main/java/mx/alness/testingapi/config/RestClientConfiguration.java
```

```java
package mx.alness.testingapi.config;

import com.alness.universalrestclient.api.RestClientCustomizer;
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

Los `RestClientCustomizer` se aplican antes de crear el cliente automático. Si
la aplicación declara un bean propio de tipo `RestClient`, la autoconfiguración
no crea otro.

## Java sin Spring

```java
RestClient client = RestClients.create();
```

Para configurar el cliente manualmente:

```java
RestClient client = RestClients.builder()
        .config(RestClientConfig.builder()
                .connectTimeoutMillis(5_000)
                .readTimeoutMillis(10_000)
                .build())
        .build();
```

## Android con Gradle

```kotlin
dependencies {
    implementation("com.alness:universal-rest-client:1.1.1")
}
```

```kotlin
val client = RestClients.create()
```

En Android utiliza `executeAsync` o la extensión Kotlin `await`; no ejecutes
red directamente en el hilo principal.

La configuración completa con `@HiltAndroidApp`, `NetworkModule`, singleton de
Hilt y repositorio suspend se encuentra en el
[ejemplo Android](examples/android/README.md).

## Tipos genéricos

```java
HttpResponse<ApiResponse<List<User>>> response = restClient.execute(
        request,
        new TypeRef<ApiResponse<List<User>>>() { }
);
```

## Codec personalizado

Solo es necesario cuando la aplicación quiere usar un `ObjectMapper` propio:

```java
RestClient client = RestClients.builder()
        .bodyCodec(new JacksonBodyCodec(applicationObjectMapper))
        .build();
```

También puede configurarse `GsonBodyCodec` o una implementación propia de
`BodyCodec`.

## Documentación

- [Arquitectura](docs/architecture.md)
- [Compatibilidad Android](docs/android-compatibility.md)
- [Integraciones](docs/integrations.md)
- [Política de compatibilidad](docs/compatibility-policy.md)
- [Proceso de release](docs/release.md)
- [Changelog](CHANGELOG.md)
- [Ejemplo completo Spring Boot](examples/spring-boot/README.md)
- [Ejemplo completo Android con Hilt](examples/android/README.md)

## Compilación

```shell
mvn clean verify
```

## Licencia

All Rights Reserved. Consulta [LICENSE](LICENSE).
