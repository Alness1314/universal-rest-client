# Android example

Minimum supported Android version: API 21.

```kotlin
dependencies {
    implementation("com.alness:universal-rest-client:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}
```

```kotlin
val client = RestClientBuilder.builder().build()

lifecycleScope.launch {
    val request = HttpRequest.builder()
        .method(HttpMethod.GET)
        .uri("https://api.example.com/health")
        .build()

    val response = client.await(request, TypeRef.of(String::class.java))
}
```

Declare `android.permission.INTERNET`. Never call `execute` on the main thread;
use `await` or `executeAsync`.
