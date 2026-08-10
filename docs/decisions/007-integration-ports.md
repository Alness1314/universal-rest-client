# ADR-007: Integraciones mediante puertos

- Estado: aceptada
- Fecha: 2026-08-06

Spring Boot dispone de autoconfiguración opcional y retrocede ante un bean
definido por la aplicación. Jakarta CDI, Micrometer y OpenTelemetry se integran
mediante customizers, interceptores, observadores, métricas y contexto de trazas.
El núcleo y sus contratos continúan siendo independientes de frameworks.
