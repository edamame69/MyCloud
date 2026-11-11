package com.mycloud.backend.auth.controller;

import com.mycloud.backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthRegistrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository repository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void registersUserWithHashedPasswordAndNoToken() throws Exception {
        String email = "Minh-" + UUID.randomUUID() + "@Example.com";
        long before = repository.count();
        register(" " + email + " ")
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"message\":\"Tạo tài khoản thành công\"}"))
                .andExpect(jsonPath("$.token").doesNotExist());
        assertEquals(before + 1, repository.count());
        var user = repository.findAll().stream().filter(item -> item.getEmail().equalsIgnoreCase(email)).findFirst().orElseThrow();
        assertNotNull(user.getId());
        assertEquals(email, user.getEmail());
        assertEquals("Minh", user.getFirstName());
        assertEquals("Lê", user.getLastName());
        assertNotEquals("zqxjkvbn", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("zqxjkvbn", user.getPasswordHash()));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndSurroundingWhitespace() throws Exception {
        String email = "Minh-" + UUID.randomUUID() + "@Example.com";
        register(email).andExpect(status().isCreated());
        long before = repository.count();
        register(" " + email.toLowerCase(java.util.Locale.ROOT) + " ")
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"message\":\"Email đã tồn tại\"}"));
        assertEquals(before, repository.count());
    }

    private org.springframework.test.web.servlet.ResultActions register(String email) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","firstName":" Minh ","lastName":" Lê ","password":"zqxjkvbn"}
                """.formatted(email)));
    }
}
