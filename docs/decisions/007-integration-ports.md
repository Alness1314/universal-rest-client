# ADR-007: Integraciones mediante puertos

- Estado: aceptada
- Fecha: 2026-08-06

Spring Boot, Jakarta CDI, Micrometer y OpenTelemetry no son dependencias del JAR.
Se integran mediante customizers, interceptores, observadores, métricas y contexto
de trazas. Esto conserva un solo artefacto compatible con Android.
