# Compatibility policy

## Semantic Versioning

Starting with `1.0.0`, public API compatibility follows Semantic Versioning.

- Patch releases fix defects without intentionally changing public contracts.
- Minor releases add backward-compatible capabilities.
- Major releases may remove or change public contracts.

Packages under `com.alness.universalrestclient.internal` are not public API and
may change in any release. Packages `api`, `config`, `exception`, `testing` and
the Kotlin extensions are supported public surfaces.

## Runtime baseline

- Published classes target Java 8 bytecode.
- Supported Android baseline is API 21 or later.
- Synchronous calls must not run on the Android main thread.
- Jackson, Gson and coroutines are optional integrations. Consumers using those
  APIs must declare the corresponding dependency explicitly.

## Deprecation

Whenever practical, a public API is deprecated for at least one minor release
before removal in a major release. Security fixes may require faster changes and
will be documented in the changelog.
