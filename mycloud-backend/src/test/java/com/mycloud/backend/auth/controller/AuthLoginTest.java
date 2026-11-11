package com.mycloud.backend.auth.controller;

import com.jayway.jsonpath.JsonPath;
import com.mycloud.backend.auth.service.JwtService;
import com.mycloud.backend.users.entity.User;
import com.mycloud.backend.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthLoginTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository repository;
    @Autowired private PasswordEncoder encoder;
    @Value("${mycloud.jwt.secret}") private String base64Secret;

    @Test
    void logsInIgnoringEmailCaseWithUnicodeNormalizationAndPasswordSpaces() throws Exception {
        String email = "Minh-" + UUID.randomUUID() + "@Example.com";
        User user = repository.save(new User(UUID.randomUUID(), email, "Minh", "Lê", encoder.encode(" café cloud ")));
        Instant before = Instant.now().minusSeconds(1);
        String json = login(" " + email.toLowerCase(java.util.Locale.ROOT) + " ", " cafe\u0301 cloud ")
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.accessToken");
        var decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(Base64.getDecoder().decode(base64Secret), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.ISSUER));
        var jwt = decoder.decode(token);
        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals(Duration.ofMinutes(30), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertFalse(jwt.getIssuedAt().isBefore(before));
        assertFalse(jwt.getIssuedAt().isAfter(Instant.now()));
        assertFalse(jwt.getClaims().containsKey("password"));
    }

    @Test
    void returnsSame401ForUnknownEmailAndWrongPassword() throws Exception {
        String email = "minh-" + UUID.randomUUID() + "@example.com";
        repository.save(new User(UUID.randomUUID(), email, "Minh", "Lê", encoder.encode("zqxjkvbn")));
        for (String candidate : new String[]{email, "missing-" + UUID.randomUUID() + "@example.com"}) {
            login(candidate, "wrong")
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json("{\"message\":\"Email hoặc mật khẩu không đúng\"}"))
                    .andExpect(jsonPath("$.accessToken").doesNotExist());
        }
    }

    @Test
    void loginWithCommonPasswordDoesNotCreateAnAccount() throws Exception {
        long before = repository.count();
        login("missing-" + UUID.randomUUID() + "@example.com", "password")
                .andExpect(status().isUnauthorized());
        assertEquals(before, repository.count());
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"%s"}
                """.formatted(email, password)));
    }
}
