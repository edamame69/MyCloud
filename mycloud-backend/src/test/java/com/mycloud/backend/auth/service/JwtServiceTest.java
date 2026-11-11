package com.mycloud.backend.auth.service;

import com.mycloud.backend.auth.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    // Public test fixture only; application configuration has no default signing secret.
    private static final byte[] KEY = "test-only-012345678901234567890123".getBytes(StandardCharsets.UTF_8);
    private static final Instant NOW = Instant.parse("2026-11-09T03:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final JwtService service = new JwtService(
            new JwtConfig().jwtEncoder(Base64.getEncoder().encodeToString(KEY)), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void issuesSignedTokenContainingUserIdAndThirtyMinuteLifetime() {
        var jwt = decoder(KEY, NOW).decode(service.createToken(userId));
        assertEquals("HS256", jwt.getHeaders().get("alg"));
        assertEquals("JWT", jwt.getHeaders().get("typ"));
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals(JwtService.ISSUER, jwt.getIssuer().toString());
        assertEquals(NOW, jwt.getIssuedAt());
        assertEquals(NOW.plusSeconds(1800), jwt.getExpiresAt());
        assertEquals(java.util.Set.of("iss", "sub", "iat", "exp"), jwt.getClaims().keySet());
    }

    @Test
    void rejectsTokenAfterExpiry() {
        String token = service.createToken(userId);
        assertDoesNotThrow(() -> decoder(KEY, NOW.plusSeconds(1799)).decode(token));
        assertThrows(JwtException.class, () -> decoder(KEY, NOW.plusSeconds(1801)).decode(token));
    }

    @Test
    void rejectsChangedUserIdAndWrongSigningKey() {
        String token = service.createToken(userId);
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace(userId.toString(), UUID.randomUUID().toString());
        String altered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        assertThrows(JwtException.class, () -> decoder(KEY, NOW).decode(altered));
        byte[] wrongKey = "different-test-01234567890123456789".getBytes(StandardCharsets.UTF_8);
        assertThrows(JwtException.class, () -> decoder(wrongKey, NOW).decode(token));
    }

    @Test
    void rejectsMalformedOrShortConfigurationSecretsWithoutEchoingThem() {
        assertThrows(IllegalArgumentException.class, () -> new JwtConfig().jwtEncoder("not base64!"));
        String shortSecret = Base64.getEncoder().encodeToString(new byte[31]);
        var error = assertThrows(IllegalArgumentException.class, () -> new JwtConfig().jwtEncoder(shortSecret));
        assertFalse(error.getMessage().contains(shortSecret));
    }

    private NimbusJwtDecoder decoder(byte[] secret, Instant at) {
        var decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
        var timestamp = new JwtTimestampValidator(Duration.ZERO);
        timestamp.setClock(Clock.fixed(at, ZoneOffset.UTC));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamp, new JwtIssuerValidator(JwtService.ISSUER)));
        return decoder;
    }
}
