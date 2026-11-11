package com.mycloud.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        // Up to 128 Unicode code points from registration may occupy 256 UTF-16 units.
        @NotBlank @Size(max = 256) String password
) {
    public LoginRequest {
        email = email == null ? null : email.strip();
        // Use the same normalization as registration, preserving password spaces.
        password = password == null ? null : Normalizer.normalize(password, Normalizer.Form.NFC);
    }

    @Override
    public String toString() {
        return "LoginRequest[redacted]";
    }
}
