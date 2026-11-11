package com.mycloud.backend.users.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.auth.service.JwtService;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserMeTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository repository;
    @Autowired private JwtService jwtService;

    @Test
    void registerLoginAndReadOwnProfileUsingVerifiedSubject() throws Exception {
        String email = "minh-" + UUID.randomUUID() + "@example.com";
        String token = registerAndLogin(email);
        User user = repository.findByEmailIgnoreCase(email).orElseThrow();
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token)
                        .param("userId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Minh"))
                .andExpect(jsonPath("$.lastName").value("Lê"))
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void readsCurrentDatabaseValuesAfterTokenWasIssued() throws Exception {
        String email = "minh-" + UUID.randomUUID() + "@example.com";
        String token = registerAndLogin(email);
        User user = repository.findByEmailIgnoreCase(email).orElseThrow();
        repository.saveAndFlush(new User(user.getId(), email, "Tên mới", user.getLastName(), user.getPasswordHash()));
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Tên mới"));
    }

    @Test
    void rejectsTokenWhoseUserNoLongerExists() throws Exception {
        String token = jwtService.createToken(UUID.randomUUID());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\":\"Phiên đăng nhập không hợp lệ hoặc đã hết hạn\"}"));
    }

    private String registerAndLogin(String email) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","firstName":"Minh","lastName":"Lê","password":"zqxjkvbn"}
                """.formatted(email))).andExpect(status().isCreated());
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"zqxjkvbn"}
                """.formatted(email))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }
}
