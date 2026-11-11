package com.mycloud.backend.auth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {
    @Test
    void hashesLongUnicodePasswordsWithoutTruncatingAndUsesFreshSalt() {
        var encoder = new PasswordConfig().passwordEncoder();
        String password = "🌱".repeat(64);
        String first = encoder.encode(password);
        String second = encoder.encode(password);

        assertTrue(first.startsWith("$argon2id$"));
        assertNotEquals(password, first);
        assertNotEquals(first, second);
        assertTrue(encoder.matches(password, first));
        assertFalse(encoder.matches(password + "x", first));
    }
}
