# Integraciones opcionales

Universal REST Client se mantiene como un solo JAR portable. Los frameworks se
conectan mediante `RestClientCustomizer`, `ExchangeObserver`,
`MetricsCollector`, `TraceContextProvider` y los contratos de codec.

## Spring Boot

```java
@Configuration
class RestClientConfiguration {
    @Bean
    RestClient universalRestClient(List<RestClientCustomizer> customizers) {
        RestClientBuilder builder = RestClientBuilder.builder();
        customizers.forEach(builder::apply);
        return builder.build();
    }
}
```

Spring es responsabilidad de la aplicación y no se agrega al artefacto portable.

## Jakarta CDI

```java
@Produces
@ApplicationScoped
RestClient universalRestClient() {
    return RestClientBuilder.builder().build();
}
```

## Micrometer

Implemente `MetricsCollector` usando `MeterRegistry` y regístrelo así:

```java
builder.addObserver(new MetricsObserver(micrometerCollector));
```

La URI recibida está sanitizada y no incluye credenciales.

## OpenTelemetry

Implemente `TraceContextProvider` a partir del span actual:

```java
builder.addRequestInterceptor(new W3cTraceContextInterceptor(provider));
```

## Gson

Gson es opcional. La aplicación que lo elija debe declarar Gson y configurar:

```java
builder.bodyCodec(new GsonBodyCodec(customGson));
```

Jackson continúa disponible mediante `JacksonBodyCodec`. Moshi puede agregarse
implementando `BodyCodec` sin modificar el transporte.
