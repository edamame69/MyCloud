package com.mycloud.backend.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() { FACTORY.close(); }

    @Test
    void trimsEmailAndNamesButPreservesPasswordSpaces() {
        var request = new RegisterRequest("  Minh@example.com  ", "  Lê Đức  ", " O’Connor-Smith ", "  café ☁ cloud  ");
        assertTrue(VALIDATOR.validate(request).isEmpty());
        assertEquals("Minh@example.com", request.email());
        assertEquals("Lê Đức", request.firstName());
        assertEquals("O’Connor-Smith", request.lastName());
        assertEquals("  café ☁ cloud  ", request.password());
        assertFalse(request.toString().contains(request.password()));
    }

    @Test
    void acceptsEightCharactersWithoutCompositionRules() {
        assertTrue(VALIDATOR.validate(request("zqxjkvbn")).isEmpty());
        assertFalse(VALIDATOR.validate(request("abcdefg")).isEmpty());
    }

    @Test
    void countsUnicodeCodePointsAndSupportsLongPasswords() {
        assertTrue(VALIDATOR.validate(request("🌱".repeat(64))).isEmpty());
        assertTrue(VALIDATOR.validate(request("🌱".repeat(128))).isEmpty());
        assertFalse(VALIDATOR.validate(request("🌱".repeat(7))).isEmpty());
        assertFalse(VALIDATOR.validate(request("🌱".repeat(129))).isEmpty());
    }

    @Test
    void normalizesPasswordUnicodeToNfc() {
        assertEquals("café cloud", request("cafe\u0301 cloud").password());
    }

    @Test
    void rejectsCommonPasswordsAndMissingValues() {
        assertFalse(VALIDATOR.validate(request("12345678")).isEmpty());
        assertFalse(VALIDATOR.validate(request("password")).isEmpty());
        assertFalse(VALIDATOR.validate(request(null)).isEmpty());
        assertFalse(VALIDATOR.validate(request("")).isEmpty());
        assertFalse(VALIDATOR.validate(request("        ")).isEmpty());
        assertFalse(VALIDATOR.validate(new RegisterRequest(null, null, null, "zqxjkvbn")).isEmpty());
    }

    @Test
    void rejectsMalformedEmailBlankNamesAndControlCharacters() {
        assertFalse(VALIDATOR.validate(new RegisterRequest("not-email", "Minh", "Lê", "zqxjkvbn")).isEmpty());
        assertFalse(VALIDATOR.validate(new RegisterRequest("minh@example.com", "   ", "Lê", "zqxjkvbn")).isEmpty());
        assertFalse(VALIDATOR.validate(new RegisterRequest("minh@example.com", "Minh", "Lê\nĐức", "zqxjkvbn")).isEmpty());
        assertFalse(VALIDATOR.validate(new RegisterRequest("minh@example.com", "a".repeat(101), "Lê", "zqxjkvbn")).isEmpty());
    }

    private RegisterRequest request(String password) {
        return new RegisterRequest("minh@example.com", "Minh", "Lê", password);
    }
}
