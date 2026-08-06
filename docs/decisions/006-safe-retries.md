# ADR-006: Reintentos seguros

- Estado: aceptada
- Fecha: 2026-08-06

Los reintentos están desactivados por defecto. Al habilitarlos, solo se repiten
métodos idempotentes y cuerpos repetibles. POST y PATCH requieren
`RetryMode.ENABLED`. Se aplica backoff exponencial, jitter, límite de intentos y
`Retry-After`. No se incorpora circuit breaker en la versión 1.0.0.
