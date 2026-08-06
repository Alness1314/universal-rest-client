# ADR-002: Compatibilidad binaria con Java 8

- Estado: aceptada
- Fecha: 2026-08-05

## Contexto

La API debe ser consumible por proyectos Java tradicionales y Android. Compilar
directamente para Java 17 reduciría el conjunto de consumidores posibles.

## Decisión

Compilar los módulos portables con `maven.compiler.release=8`. La automatización
usará JDK modernos para compilar y probar ese bytecode.

## Consecuencias

- El código público no puede utilizar records, sealed classes ni APIs posteriores
  a Java 8.
- Android puede requerir desugaring según su nivel mínimo y herramientas.
- La compatibilidad Android completa se validará junto al transporte OkHttp.
