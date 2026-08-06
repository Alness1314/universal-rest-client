package com.alness.universalrestclient.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypeRefTest {
    @Test
    void capturesGenericTypes() {
        TypeRef<List<String>> reference = new TypeRef<List<String>>() { };

        assertThat(reference.type()).isInstanceOf(ParameterizedType.class);
        assertThat(reference.toString()).contains("java.util.List").contains("java.lang.String");
    }

    @Test
    void wrapsDirectClasses() {
        assertThat(TypeRef.of(String.class).type()).isEqualTo(String.class);
    }
}
