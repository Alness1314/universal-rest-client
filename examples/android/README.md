# Ejemplo Android con Hilt y Jetpack Compose

Versión mínima soportada: Android API 21.

## Dependencias

```kotlin
dependencies {
    implementation("com.alness:universal-rest-client:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")
}
```

## Application de Hilt

```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

Registre la clase en `AndroidManifest.xml` y declare acceso a Internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application android:name=".MyApplication">
    <!-- Activities -->
</application>
```

## Cliente singleton

Coloque la configuración en una clase como:

```text
app/src/main/java/mx/alness/myapplication/di/NetworkModule.kt
```

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRestClient(): RestClient = RestClients.builder()
        .config(
            RestClientConfig.builder()
                .connectTimeoutMillis(5_000)
                .readTimeoutMillis(10_000)
                .errorPolicy(HttpErrorPolicy.THROW_ON_4XX_5XX)
                .build()
        )
        .retryPolicy(
            RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelayMillis(100)
                .maxDelayMillis(2_000)
                .build()
        )
        .addRequestInterceptor(UserAgentInterceptor("MyApplication/1.0"))
        .build()
}
```

## Repositorio

```kotlin
@Singleton
class PokemonRepository @Inject constructor(
    private val restClient: RestClient
) {
    suspend fun findByNumber(number: Int): PokemonResponse {
        val request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .uri("https://pokeapi.co/api/v2/pokemon/$number")
            .build()

        return requireNotNull(
            restClient.await(request, TypeRef.of(PokemonResponse::class.java)).body()
        )
    }
}
```

Use el repositorio desde un `@HiltViewModel` y llame la función suspend dentro
de `viewModelScope`. Nunca ejecute `RestClient.execute` en el hilo principal.
