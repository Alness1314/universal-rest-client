# Changelog

All notable changes follow Semantic Versioning.

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
