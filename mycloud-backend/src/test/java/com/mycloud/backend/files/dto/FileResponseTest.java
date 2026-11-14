package com.mycloud.backend.files.dto;

import com.mycloud.backend.files.entity.FileMetadata;
import com.mycloud.backend.folders.entity.Folder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileResponseTest {

    @Test
    void buildsCurrentPathFromFolderAncestors() {
        Folder root = new Folder(UUID.randomUUID(), "Hình", null);
        Folder child = new Folder(UUID.randomUUID(), "2026", root);
        FileMetadata file = new FileMetadata(UUID.randomUUID(), "minh.png", Instant.now(), Instant.now(),
                10, "image/png", child);

        FileResponse response = FileResponse.from(file);

        assertEquals(child.getId(), response.folderId());
        assertEquals("Hình/2026/minh.png", response.path());
    }

    @Test
    void usesOnlyFileNameForRootFile() {
        FileMetadata file = new FileMetadata(UUID.randomUUID(), "minh.png", Instant.now(), Instant.now(),
                10, "image/png", null);

        FileResponse response = FileResponse.from(file);

        assertNull(response.folderId());
        assertEquals("minh.png", response.path());
    }
}
