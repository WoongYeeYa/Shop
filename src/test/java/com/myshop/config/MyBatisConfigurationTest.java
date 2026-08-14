package com.myshop.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MyBatisConfigurationTest {
    @Test
    void loadsAllMapperDefinitions() {
        assertNotNull(MyBatisProvider.getFactory());
    }
}
