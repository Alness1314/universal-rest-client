# ADR-004: OkHttp como transporte

- Estado: aceptada
- Fecha: 2026-08-05

## Contexto

El cliente necesita un transporte reutilizable en Java y Android, con pooling,
timeouts, cancelación y soporte moderno de HTTP/TLS.

## Decisión

Usar OkHttp 5.3.0 como dependencia de producción. En Maven se selecciona
explícitamente el artefacto JVM (`okhttp-jvm`) porque el artefacto `okhttp`
publica metadatos multiplataforma orientados a herramientas con resolución de
variantes.

La aplicación crea el transporte mediante `RestClientBuilder`, sin interactuar
con clases internas. Una instancia del cliente se comparte entre llamadas.

## Consecuencias

- Android mínimo declarado por OkHttp: API 21.
- Java mínimo declarado por OkHttp: Java 8.
- Kotlin estándar y Okio son dependencias transitivas.
- La implementación conserva un único pool por cliente y permite cancelación
  individual mediante `RestCall`.
