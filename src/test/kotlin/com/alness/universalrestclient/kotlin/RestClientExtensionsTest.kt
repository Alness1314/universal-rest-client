package com.alness.universalrestclient.kotlin

import com.alness.universalrestclient.api.HttpMethod
import com.alness.universalrestclient.api.HttpRequest
import com.alness.universalrestclient.api.TypeRef
import com.alness.universalrestclient.testing.HttpResponses
import com.alness.universalrestclient.testing.StubRestClient
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RestClientExtensionsTest {
    @Test
    fun `await returns the asynchronous response`() {
        runBlocking {
            val client = StubRestClient().enqueueResponse(HttpResponses.successful("done"))
            val request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .uri("https://example.test/kotlin")
                .build()

            val response = client.await(request, TypeRef.of(String::class.java))

            assertThat(response.body()).isEqualTo("done")
        }
    }
}
