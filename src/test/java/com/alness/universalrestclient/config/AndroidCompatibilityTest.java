package com.alness.universalrestclient.config;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class AndroidCompatibilityTest {
    @Test
    void projectAndTransportUseJava8Bytecode() throws Exception {
        assertThat(majorVersion(RestClientBuilder.class)).isEqualTo(52);
        assertThat(majorVersion(OkHttpClient.class)).isEqualTo(52);
    }

    @Test
    void packagesConsumerR8Rules() {
        assertThat(RestClientBuilder.class.getClassLoader().getResource(
                "META-INF/proguard/universal-rest-client.pro")).isNotNull();
    }

    private static int majorVersion(Class<?> type) throws Exception {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream input = type.getResourceAsStream(resource);
             DataInputStream data = new DataInputStream(input)) {
            assertThat(data.readInt()).isEqualTo(0xCAFEBABE);
            data.readUnsignedShort();
            return data.readUnsignedShort();
        }
    }
}
