package com.mycloud.backend.auth.config;

import com.mycloud.backend.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtDecoderTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(
            "public-test-only-01234567890123456".getBytes(StandardCharsets.UTF_8));
    private static final Instant NOW = Instant.parse("2026-11-09T09:00:00Z");
    private final JwtConfig config = new JwtConfig();

    @Test
    void configuredDecoderVerifiesTokenIssuedByJwtService() {
        UUID id = UUID.randomUUID();
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        String token = new JwtService(config.jwtEncoder(SECRET), clock).createToken(id);
        var jwt = config.jwtDecoder(SECRET, clock).decode(token);
        assertEquals(id.toString(), jwt.getSubject());
        assertEquals(NOW.plusSeconds(1800), jwt.getExpiresAt());
    }

    @Test
    void configuredDecoderEnforcesExpiryWithoutClockLeeway() {
        String token = new JwtService(config.jwtEncoder(SECRET), Clock.fixed(NOW, ZoneOffset.UTC))
                .createToken(UUID.randomUUID());
        assertDoesNotThrow(() -> config.jwtDecoder(SECRET, Clock.fixed(NOW.plusSeconds(1799), ZoneOffset.UTC)).decode(token));
        assertThrows(JwtException.class,
                () -> config.jwtDecoder(SECRET, Clock.fixed(NOW.plusSeconds(1801), ZoneOffset.UTC)).decode(token));
    }
}
