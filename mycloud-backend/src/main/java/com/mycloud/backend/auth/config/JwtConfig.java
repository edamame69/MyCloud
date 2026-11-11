package com.mycloud.backend.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;
import com.mycloud.backend.auth.service.JwtService;

import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtConfig {
    @Bean
    public JwtEncoder jwtEncoder(@Value("${mycloud.jwt.secret}") String base64Secret) {
        return NimbusJwtEncoder.withSecretKey(signingKey(base64Secret))
                .algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${mycloud.jwt.secret}") String base64Secret, Clock clock) {
        // Signature verification happens inside Nimbus; explicitly accept only HS256.
        var decoder = NimbusJwtDecoder.withSecretKey(signingKey(base64Secret))
                .macAlgorithm(MacAlgorithm.HS256).build();
        var timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setClock(clock);
        timestamps.setAllowEmptyExpiryClaim(false);
        timestamps.setAllowEmptyNotBeforeClaim(true);
        OAuth2TokenValidator<Jwt> subject = jwt -> {
            String value = jwt.getSubject();
            if (value != null) {
                try {
                    if (UUID.fromString(value).toString().equalsIgnoreCase(value)) {
                        return OAuth2TokenValidatorResult.success();
                    }
                } catch (IllegalArgumentException ignored) {
                    // A malformed subject is an invalid token, not a server error.
                }
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid subject", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                timestamps, new JwtIssuerValidator(JwtService.ISSUER), subject));
        return decoder;
    }

    private SecretKeySpec signingKey(String base64Secret) {
        byte[] secret;
        try {
            secret = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            // Do not attach a cause that might include the supplied secret.
            throw new IllegalArgumentException("MYCLOUD_JWT_SECRET must be Base64-encoded");
        }
        if (secret.length < 32) {
            throw new IllegalArgumentException("MYCLOUD_JWT_SECRET must contain at least 32 random bytes");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    public Clock jwtClock() {
        return Clock.systemUTC();
    }
}
