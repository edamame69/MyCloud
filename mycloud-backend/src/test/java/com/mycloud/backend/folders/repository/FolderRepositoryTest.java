package com.mycloud.backend.folders.repository;

import com.mycloud.backend.folders.entity.Folder;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Test
    void checksNamesWithoutCaseOnlyWithinTheSameParent() {
        UUID id = UUID.randomUUID();
        User owner = userRepository.save(new User(id, id + "@example.com", "Test", "Owner", "test-hash"));
        Folder photos = folderRepository.save(new Folder(UUID.randomUUID(), "Ảnh", null, owner));
        Folder other = folderRepository.save(new Folder(UUID.randomUUID(), "Khác", null, owner));
        folderRepository.save(new Folder(UUID.randomUUID(), "2026", photos, owner));

        assertTrue(folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, null, "ảnh"));
        assertTrue(folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, photos, "2026"));
        assertFalse(folderRepository.existsByOwnerAndParentAndNameIgnoreCase(owner, other, "2026"));
    }
}
