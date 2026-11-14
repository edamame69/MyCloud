package com.mycloud.backend.folders.repository;

import com.mycloud.backend.folders.entity.Folder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FolderRepositoryTest {

    @Autowired
    private FolderRepository folderRepository;

    @Test
    void checksNamesWithoutCaseOnlyWithinTheSameParent() {
        Folder photos = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null));
        Folder other = folderRepository.save(new Folder(UUID.randomUUID(), "Khác", null));
        folderRepository.save(new Folder(UUID.randomUUID(), "2026", photos));

        assertTrue(folderRepository.existsByParentAndNameIgnoreCase(null, "ảnh"));
        assertTrue(folderRepository.existsByParentAndNameIgnoreCase(photos, "2026"));
        assertFalse(folderRepository.existsByParentAndNameIgnoreCase(other, "2026"));
    }
}
