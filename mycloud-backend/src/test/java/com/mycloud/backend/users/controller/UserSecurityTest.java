package com.mycloud.backend.users.controller;

import com.mycloud.backend.auth.config.JwtConfig;
import com.mycloud.backend.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Rejection paths can run before learner finishes the successful /me path.
@SpringBootTest
@AutoConfigureMockMvc
class UserSecurityTest {
    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;

    @Test
    void rejectsMissingBearerToken() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().json("{\"message\":\"Phiên đăng nhập không hợp lệ hoặc đã hết hạn\"}"));
    }

    @Test
    void rejectsMalformedToken() throws Exception {
        assertUnauthorized("invalid-token");
    }

    @Test
    void rejectsExpiredSignedToken() throws Exception {
        var claims = claims();
        claims.put("iat", Instant.now().minusSeconds(2000));
        claims.put("exp", Instant.now().minusSeconds(1));
        assertUnauthorized(sign(encoder, claims));
    }

    @Test
    void rejectsWrongSignature() throws Exception {
        String otherKey = Base64.getEncoder().encodeToString(
                "other-public-test-key-01234567890".getBytes(StandardCharsets.UTF_8));
        var otherEncoder = new JwtConfig().jwtEncoder(otherKey);
        assertUnauthorized(sign(otherEncoder, claims()));
    }

    @Test
    void rejectsWrongIssuerMissingExpiryAndInvalidSubjects() throws Exception {
        var wrongIssuer = claims();
        wrongIssuer.put("iss", "https://another-issuer.invalid");
        assertUnauthorized(sign(encoder, wrongIssuer));
        var missingExpiry = claims();
        missingExpiry.remove("exp");
        assertUnauthorized(sign(encoder, missingExpiry));
        var missingSubject = claims();
        missingSubject.remove("sub");
        assertUnauthorized(sign(encoder, missingSubject));
        var badSubject = claims();
        badSubject.put("sub", "not-a-uuid");
        assertUnauthorized(sign(encoder, badSubject));
        var futureToken = claims();
        futureToken.put("nbf", Instant.now().plusSeconds(120));
        assertUnauthorized(sign(encoder, futureToken));
    }

    private void assertUnauthorized(String token) throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().json("{\"message\":\"Phiên đăng nhập không hợp lệ hoặc đã hết hạn\"}"));
    }

    private Map<String, Object> claims() {
        return new HashMap<>(Map.of("iss", JwtService.ISSUER, "sub", UUID.randomUUID().toString(),
                "iat", Instant.now(), "exp", Instant.now().plusSeconds(1800)));
    }

    private String sign(JwtEncoder signer, Map<String, Object> claims) {
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return signer.encode(JwtEncoderParameters.from(header,
                JwtClaimsSet.builder().claims(values -> values.putAll(claims)).build())).getTokenValue();
    }
}
