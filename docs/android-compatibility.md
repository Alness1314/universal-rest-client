# Compatibilidad inicial con Android

## Alcance verificado en la etapa 2

- El código propio se compila con `--release 8`.
- OkHttp 5.3.0 declara soporte para Java 8 y Android 5.0 o posterior
  (`minSdk 21`).
- OkHttp incluye reglas para R8 en su artefacto.
- La API pública no importa clases de Android.
- La implementación no realiza operaciones de red automáticamente en el hilo
  principal; el consumidor decide en qué hilo ejecuta la llamada síncrona.

Fuentes oficiales:

- [Requisitos de OkHttp](https://github.com/square/okhttp#requirements)
- [R8 y ProGuard](https://square.github.io/okhttp/features/r8_proguard/)

## Consideraciones

- OkHttp 5 utiliza Kotlin y Okio como dependencias transitivas.
- En Android, OkHttp utiliza AndroidX Startup. Si la aplicación deshabilita su
  inicializador, debe inicializar OkHttp desde `Application.onCreate` como indica
  la documentación oficial.
- La validación instrumental en un dispositivo o emulador Android queda para la
  etapa específica de integración Android.
- `RestClient.execute` es síncrono y no debe ejecutarse en el hilo principal de
  Android. La API asíncrona se incorporará en una etapa posterior.
