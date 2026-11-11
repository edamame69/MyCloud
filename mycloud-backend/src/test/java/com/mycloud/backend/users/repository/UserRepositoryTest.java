package com.mycloud.backend.users.repository;

import com.mycloud.backend.users.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {
    @Autowired
    private UserRepository repository;

    @Test
    void findsEmailIgnoringCase() {
        String suffix = UUID.randomUUID().toString();
        User saved = repository.saveAndFlush(new User(UUID.randomUUID(), "Minh-" + suffix + "@Example.com", "Minh", "Lê", "test-hash"));
        assertTrue(repository.existsByEmailIgnoreCase("minh-" + suffix + "@example.com"));
        assertFalse(repository.existsByEmailIgnoreCase("missing-" + suffix + "@example.com"));
        assertEquals(saved.getId(), repository.findByEmailIgnoreCase("minh-" + suffix + "@example.com").orElseThrow().getId());
        assertEquals("test-hash", repository.findByEmailIgnoreCase("minh-" + suffix + "@example.com").orElseThrow().getPasswordHash());
        assertTrue(repository.findByEmailIgnoreCase("missing-" + suffix + "@example.com").isEmpty());
    }
}
