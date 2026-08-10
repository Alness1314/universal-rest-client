# Changelog

All notable changes follow Semantic Versioning.

## 1.1.1 - 2026-08-10

### Fixed

- Spring Boot auto-configuration now compiles against a Java 8-compatible API baseline.
- Spring remains optional and is not pulled into Java or Android consumers.
- Restored successful CI verification on JDK 11 while retaining Spring Boot 4 runtime compatibility.

## 1.1.0 - 2026-08-09

### Changed

- Jackson JSON support is ready by default and is included transitively.
- Added `RestClients.create()` as the zero-configuration Java and Android entry point.
- Added optional Spring Boot auto-configuration for direct `RestClient` injection.
- Applications can still replace the codec, customize the builder or provide their own bean.

## 1.0.0 - 2026-08-06

### Added

- Java 8 and Android-compatible synchronous HTTP client backed by OkHttp.
- Immutable requests, responses, headers and configuration.
- GET, POST, PUT, PATCH, DELETE, HEAD and OPTIONS.
- Text, JSON, byte array and repeatable stream request bodies.
- Jackson and Gson codecs with generic `TypeRef<T>` support.
- Structured errors, status policy, response limits and secret redaction.
- Authentication, metadata, tracing and response interceptors.
- Safe metadata logging and metrics lifecycle observers.
- Bounded idempotency-aware retries with exponential backoff and `Retry-After`.
- Cancellable `CompletableFuture` API and Kotlin `suspend` extension.
- R8 consumer rules, test utilities, JavaDoc, examples and release automation.

### Compatibility

- Java bytecode level 8.
- Android API 21 or later, inherited from OkHttp 5.3.0.
