package com.mycloud.backend.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private final Set<String> commonPasswords;

    public PasswordValidator() {
        try (var reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("auth/common-passwords.txt").getInputStream(), StandardCharsets.UTF_8))) {
            commonPasswords = reader.lines().collect(Collectors.toUnmodifiableSet());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load the common-password list", exception);
        }
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }
        int length = password.codePointCount(0, password.length());
        return length >= 8 && length <= 128 && !commonPasswords.contains(password);
    }
}
