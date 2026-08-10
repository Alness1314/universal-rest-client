# ADR-001: Arquitectura neutral en un solo artefacto

- Estado: aceptada
- Fecha: 2026-08-05

## Contexto

La librería debe funcionar en aplicaciones Java y Android sin obligarlas a
incorporar Spring Boot, Feign o una implementación JSON determinada. En su etapa
inicial no existe suficiente complejidad para justificar varios artefactos.

## Decisión

Publicar un solo JAR y separar contratos, configuración, errores, implementación
interna y utilidades de prueba mediante paquetes. Las integraciones de framework
deben ser opcionales y mantenerse fuera de los contratos del núcleo.

## Consecuencias

- Los consumidores agregan una sola dependencia.
- La compilación y publicación son más sencillas.
- Los contratos deben mantenerse pequeños y estables.
- Las dependencias opcionales futuras se evaluarán antes de incorporarse para no
  convertirlas accidentalmente en obligatorias.
