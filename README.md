# Universal REST Client

Cliente HTTP reutilizable para Java y Android, distribuido como un único JAR y
sin dependencia de Spring Boot.

## Características

- Java 8 y Android API 21+.
- Transporte OkHttp reutilizable y thread-safe.
- GET, POST, PUT, PATCH, DELETE, HEAD y OPTIONS.
- Requests, responses, headers y configuración inmutables.
- Texto, JSON, bytes y streams repetibles.
- Jackson o Gson opcionales, incluyendo tipos genéricos.
- Errores estructurados y política configurable para estados HTTP.
- Redacción de tokens, cookies y API keys.
- Interceptores, autenticación dinámica, correlation ID y W3C Trace Context.
- Reintentos idempotentes con backoff, jitter y `Retry-After`.
- API síncrona, `CompletableFuture` cancelable y extensión Kotlin `suspend`.
- Métricas y customización mediante contratos neutrales.

## Dependencia

```xml
<dependency>
    <groupId>com.alness</groupId>
    <artifactId>universal-rest-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

GitHub Packages requiere configurar un repositorio Maven y credenciales con id
`github`. Consulta [el proceso de release](docs/release.md).

El proyecto publica un único artefacto, por lo que no necesita BOM. Jackson,
Gson y coroutines están marcados como opcionales y deben declararse cuando se
usen sus adaptadores.

## Uso básico

```java
RestClient client = RestClientBuilder.builder()
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
        .build();

HttpRequest request = HttpRequest.builder()
        .method(HttpMethod.GET)
        .uri("https://api.example.com/users")
        .addQueryParameter("active", "true")
        .build();

HttpResponse<String> response = client.execute(request, TypeRef.of(String.class));
```

## JSON con Jackson

La aplicación declara `jackson-databind` y `jackson-datatype-jsr310` 2.22.0:

```java
RestClient client = RestClientBuilder.builder()
        .bodyCodec(new JacksonBodyCodec(applicationObjectMapper))
        .build();

HttpResponse<ApiResponse<List<User>>> response = client.execute(
        request,
        new TypeRef<ApiResponse<List<User>>>() { }
);
```

También puede utilizarse `GsonBodyCodec` o una implementación propia de
`BodyCodec` para Moshi u otro serializador.

## Cuerpos

```java
HttpBodies.text("contenido");
HttpBodies.json("{\"active\":true}");
HttpBodies.bytes(binaryData);
HttpBodies.stream(inputStreamSupplier, contentLength, "application/octet-stream");
```

Los objetos tipados pueden agregarse directamente al request y serán procesados
por el codec configurado:

```java
HttpRequest request = HttpRequest.builder()
        .method(HttpMethod.POST)
        .uri(url)
        .body(command, TypeRef.of(CreateUserCommand.class))
        .build();
```

## Autenticación e interceptores

```java
RestClient client = RestClientBuilder.builder()
        .addRequestInterceptor(new BearerTokenInterceptor(session::currentToken))
        .addRequestInterceptor(new ApiKeyInterceptor("X-API-Key", keys::current))
        .addRequestInterceptor(new CorrelationIdInterceptor())
        .addRequestInterceptor(new UserAgentInterceptor("my-application/1.0"))
        .build();
```

`SafeLoggingObserver` registra únicamente metadatos y redacta credenciales. No
registra cuerpos.

## Reintentos

Los reintentos están desactivados por defecto. GET, HEAD, OPTIONS, PUT y DELETE
pueden repetirse automáticamente cuando se habilitan. POST y PATCH requieren
autorización explícita:

```java
request.toBuilder().retryMode(RetryMode.ENABLED).build();
```

No se repiten cuerpos marcados como no repetibles.

## API asíncrona y Kotlin

```java
CompletableFuture<HttpResponse<String>> future =
        client.executeAsync(request, TypeRef.of(String.class));

future.cancel(true); // cancela también la operación HTTP
```

```kotlin
val response = client.await(request, TypeRef.of(String::class.java))
```

El ejecutor puede inyectarse mediante `RestClientBuilder.executor`. La librería
no crea ni destruye ejecutores pertenecientes a la aplicación.

## Documentación

- [Arquitectura](docs/architecture.md)
- [Compatibilidad Android](docs/android-compatibility.md)
- [Integraciones](docs/integrations.md)
- [Política de compatibilidad](docs/compatibility-policy.md)
- [Proceso de release](docs/release.md)
- [Decisiones arquitectónicas](docs/decisions/README.md)
- [Changelog](CHANGELOG.md)

## Compilación

```shell
mvn clean verify
```

Para comprobar el proyecto consumidor independiente:

```shell
mvn install
mvn -f examples/java/pom.xml test
```

## Licencia

All Rights Reserved. Consulta [LICENSE](LICENSE).
