# Integraciones opcionales

Universal REST Client se mantiene como un solo JAR portable. Los frameworks se
conectan mediante contratos opcionales y no son necesarios en Java o Android.

## Spring Boot

Spring Boot detecta la autoconfiguración y crea un bean `RestClient` cuando la
aplicación no proporciona uno:

```java
@Service
class RemoteService {
    private final RestClient restClient;

    RemoteService(RestClient restClient) {
        this.restClient = restClient;
    }
}
```

La preconfiguración se realiza mediante beans `RestClientCustomizer`. Un bean
`RestClient` definido por la aplicación reemplaza el predeterminado.

## Jakarta CDI

```java
@Produces
@ApplicationScoped
RestClient universalRestClient() {
    return RestClients.create();
}
```

## Micrometer

Implemente `MetricsCollector` usando `MeterRegistry` y regístrelo así:

```java
builder.addObserver(new MetricsObserver(micrometerCollector));
```

## OpenTelemetry

Implemente `TraceContextProvider` a partir del span actual:

```java
builder.addRequestInterceptor(new W3cTraceContextInterceptor(provider));
```

## Gson

Gson es opcional y reemplaza el codec predeterminado cuando se configura:

```java
builder.bodyCodec(new GsonBodyCodec(customGson));
```
