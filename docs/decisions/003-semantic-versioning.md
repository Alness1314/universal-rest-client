# ADR-003: Versionado semántico

- Estado: aceptada
- Fecha: 2026-08-05

## Decisión

Usar versionado semántico y comenzar en `0.1.0-SNAPSHOT`. Las versiones `0.x`
indican que la API todavía puede evolucionar. Antes de `1.0.0`, todo cambio
incompatible deberá quedar identificado en las notas de la versión.

## Política

- No publicar artefactos release con sufijo `SNAPSHOT`.
- Mantener una sola versión para el artefacto completo.
- Incrementar `MAJOR` después de `1.0.0` cuando cambie un contrato público de
  forma incompatible.
