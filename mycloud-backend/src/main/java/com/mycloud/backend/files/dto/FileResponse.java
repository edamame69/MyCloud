package com.mycloud.backend.files.dto;

import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.folders.entity.Folder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        long sizeBytes,
        String contentType,
        UUID folderId,
        String path
) {
    public static FileResponse from(FileMetadata file) {
        Folder folder = file.getFolder();
        UUID folderId = null;
        if(folder != null) folderId = folder.getId();
        List<String> segments = new ArrayList<>();
        while (folder != null) {
            segments.add(folder.getName());
            folder = folder.getParent();
        }
        Collections.reverse(segments);
        segments.add(file.getName());

        return new FileResponse(
                file.getId(),
                file.getName(),
                file.getCreatedAt(),
                file.getUpdatedAt(),
                file.getSizeBytes(),
                file.getContentType(),
                folderId,
                String.join("/", segments)
        );
    }
}
