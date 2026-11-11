package com.mycloud.backend.auth.service;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {
    public static final long LIFETIME_SECONDS = 1800;
    // Identifier only; issuing tokens does not contact this URL.
    public static final String ISSUER = "https://mycloud.local";

    private final JwtEncoder encoder;
    private final Clock clock;

    public JwtService(JwtEncoder encoder, Clock clock) {
        this.encoder = encoder;
        this.clock = clock;
    }

    public String createToken(UUID userId) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(LIFETIME_SECONDS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
