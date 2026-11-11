package com.mycloud.backend.auth.dto;

import com.mycloud.backend.auth.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[^\\p{Cntrl}]*") String firstName,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[^\\p{Cntrl}]*") String lastName,
        @ValidPassword String password
) {
    public RegisterRequest {
        email = email == null ? null : email.strip();
        firstName = firstName == null ? null : firstName.strip();
        lastName = lastName == null ? null : lastName.strip();
        // Preserve password spaces; login must apply the same NFC normalization.
        password = password == null ? null : Normalizer.normalize(password, Normalizer.Form.NFC);
    }

    @Override
    public String toString() {
        return "RegisterRequest[redacted]";
    }
}
