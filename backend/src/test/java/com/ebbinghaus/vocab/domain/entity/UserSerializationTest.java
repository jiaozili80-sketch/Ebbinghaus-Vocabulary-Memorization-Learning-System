package com.ebbinghaus.vocab.domain.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void neverSerializesPasswordHash() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setPassword("bcrypt-hash");

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("bcrypt-hash"));
    }
}
