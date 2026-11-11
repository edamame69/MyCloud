package com.mycloud.backend.folders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateFolderRequest(
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[^.:/\\p{Cntrl}][^:/\\p{Cntrl}]*$")
        String name,
        UUID parentId
) {
}
