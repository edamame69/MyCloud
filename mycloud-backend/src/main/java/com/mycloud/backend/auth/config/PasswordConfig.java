package com.mycloud.backend.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2id: salt 16 bytes, hash 32 bytes, parallelism 1, memory 19 MiB, passes 2.
        return new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2);
    }
}
