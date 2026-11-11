package com.mycloud.backend.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() { FACTORY.close(); }

    @Test
    void normalizesLikeRegistrationWithoutRemovingPasswordSpaces() {
        var request = new LoginRequest(" Minh@example.com ", " cafe\u0301 cloud ");
        assertEquals("Minh@example.com", request.email());
        assertEquals(" café cloud ", request.password());
        assertTrue(VALIDATOR.validate(request).isEmpty());
        assertFalse(request.toString().contains(request.password()));
    }

    @Test
    void doesNotApplyRegistrationStrengthRules() {
        // A nonblank but wrong password is an authentication failure, not a format error.
        assertTrue(VALIDATOR.validate(new LoginRequest("minh@example.com", "x")).isEmpty());
        assertTrue(VALIDATOR.validate(new LoginRequest("minh@example.com", "password")).isEmpty());
    }

    @Test
    void rejectsMissingValuesAndMalformedEmails() {
        assertFalse(VALIDATOR.validate(new LoginRequest(null, null)).isEmpty());
        assertFalse(VALIDATOR.validate(new LoginRequest("wrong", "zqxjkvbn")).isEmpty());
        assertFalse(VALIDATOR.validate(new LoginRequest("minh@example.com", "   ")).isEmpty());
    }

    @Test
    void acceptsAllRegisteredPasswordLengthsButBoundsInput() {
        assertTrue(VALIDATOR.validate(new LoginRequest("minh@example.com", "🌱".repeat(128))).isEmpty());
        assertFalse(VALIDATOR.validate(new LoginRequest("minh@example.com", "x".repeat(257))).isEmpty());
    }
}
