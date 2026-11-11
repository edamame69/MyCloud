package com.mycloud.backend.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateFileRequest(
        @NotBlank @Size(max = 255)
        @Pattern(regexp = "^[^.:/\\p{Cntrl}][^:/\\p{Cntrl}]*$")
        String name,
        @NotNull @PositiveOrZero Long sizeBytes,
        @NotBlank String contentType,
        UUID folderId
) {
}
