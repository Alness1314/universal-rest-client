# Arquitectura

## Objetivo

Universal REST Client proporciona una API HTTP para Java y Android sin depender
de un framework. OkHttp es el transporte integrado y los serializadores son
intercambiables.

## Organización interna

```text
Aplicación consumidora
        |
        v
universal-rest-client.jar
        |
        +-- api
        +-- config
        +-- exception
        +-- internal
        +-- testing
        +-- kotlin
```

Los paquetes mantienen responsabilidades separadas, pero se compilan y publican
como un solo artefacto. Las dependencias opcionales futuras no deben filtrarse a
los contratos públicos.

## Responsabilidades

### `api`

- Representar solicitudes y respuestas inmutables.
- Conservar códigos HTTP, headers, cuerpo tipado y cuerpo original.
- Representar tipos genéricos mediante `TypeRef<T>`.
- Definir el contrato síncrono de `RestClient`.

### `config`

- Alojar configuración compartida por transportes futuros.
- Validar límites y timeouts.
- No abrir conexiones ni seleccionar un serializador.

### `exception`

- Exponer errores de solicitud, transporte, serialización y estado HTTP.

### `internal`

- Alojar detalles que no forman parte del contrato público.
- No debe ser usado directamente por aplicaciones consumidoras.
- Contiene el adaptador OkHttp, reutilizado por el builder público.

### `testing`

- Permitir pruebas unitarias sin acceso a red.
- Registrar solicitudes ejecutadas por el consumidor.
- Entregar respuestas y errores previamente programados.

## Principios

1. La librería no conoce Spring ni Android.
2. Los objetos públicos que representan datos son inmutables.
3. Headers y cuerpos binarios se copian defensivamente.
4. Las URLs se representan con `URI`, no se dividen manualmente.
5. Las respuestas vacías son válidas.
6. La información HTTP no se pierde al decodificar una respuesta.
7. La separación por paquetes no obliga a publicar múltiples artefactos.

## Capas de ejecución

```text
ExecutorRestClient
        |
RetryingRestClient (cuando está habilitado)
        |
OkHttpRestClient
        |
OkHttp / pool de conexiones
```

Spring, CDI, Micrometer y OpenTelemetry se conectan mediante puertos públicos y
permanecen fuera del núcleo portable.
