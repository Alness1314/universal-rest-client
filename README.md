# Universal REST Client

Cliente HTTP reutilizable para Java, Android y Spring Boot. Se distribuye como
un único JAR y usa OkHttp con JSON de Jackson listo para utilizar.

## Características

- Java 8 y Android API 21+.
- Cliente reutilizable y thread-safe.
- JSON habilitado por defecto, incluyendo fechas y tipos genéricos.
- Errores estructurados, interceptores, autenticación y logging seguro.
- Reintentos idempotentes con backoff, jitter y `Retry-After`.
- API síncrona, `CompletableFuture` cancelable y extensión Kotlin `suspend`.
- Autoconfiguración opcional para inyección en Spring Boot.

## Dependencia

```xml
<dependency>
    <groupId>com.alness</groupId>
    <artifactId>universal-rest-client</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Java y Android

El cliente predeterminado ya procesa JSON; no es necesario registrar Jackson:

```java
RestClient client = RestClients.create();

HttpRequest request = HttpRequest.builder()
        .method(HttpMethod.GET)
        .uri("https://api.example.com/users/1")
        .build();

HttpResponse<User> response = client.execute(request, TypeRef.of(User.class));
```

Para tipos genéricos:

```java
HttpResponse<ApiResponse<List<User>>> response = client.execute(
        request,
        new TypeRef<ApiResponse<List<User>>>() { }
);
```

Para personalizar el cliente:

```java
RestClient client = RestClients.builder()
        .config(RestClientConfig.builder()
                .connectTimeoutMillis(5_000)
                .readTimeoutMillis(10_000)
                .build())
        .retryPolicy(RetryPolicy.builder().maxAttempts(3).build())
        .build();
```

## Spring Boot

Spring Boot crea automáticamente un bean `RestClient`. Solo hay que inyectarlo:

```java
@Service
public class UserService {
    private final RestClient restClient;

    public UserService(RestClient restClient) {
        this.restClient = restClient;
    }
}
```

Para preconfigurarlo sin reemplazarlo:

```java
@Bean
RestClientCustomizer restClientDefaults() {
    return builder -> builder
            .config(RestClientConfig.builder()
                    .connectTimeoutMillis(5_000)
                    .readTimeoutMillis(10_000)
                    .build())
            .addRequestInterceptor(
                    new UserAgentInterceptor("my-application/1.0"));
}
```

Un bean `RestClient` definido por la aplicación reemplaza al predeterminado.

## Codec personalizado

```java
RestClient client = RestClients.builder()
        .bodyCodec(new JacksonBodyCodec(applicationObjectMapper))
        .build();
```

También están disponibles `GsonBodyCodec`, `RawBodyCodec` y los contratos para
agregar Moshi u otro serializador. Gson continúa siendo opcional.

## Documentación

- [Arquitectura](docs/architecture.md)
- [Compatibilidad Android](docs/android-compatibility.md)
- [Integraciones](docs/integrations.md)
- [Política de compatibilidad](docs/compatibility-policy.md)
- [Proceso de release](docs/release.md)
- [Changelog](CHANGELOG.md)

## Compilación

```shell
mvn clean verify
```

## Licencia

All Rights Reserved. Consulta [LICENSE](LICENSE).
