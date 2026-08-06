# ADR-005: Serialización intercambiable

- Estado: aceptada
- Fecha: 2026-08-06

El transporte depende de `BodyCodec`, no de Jackson o Gson. Ambos adaptadores
son opcionales y aceptan instancias configuradas por la aplicación. El codec raw
permanece disponible para `String`, `byte[]` y `Void`.
