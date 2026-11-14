package com.mycloud.backend.auth.config;

import com.mycloud.backend.auth.security.JsonSecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain usersSecurityFilterChain(HttpSecurity http, JsonSecurityErrorHandler errors) throws Exception {
        return http
                // First JWT integration is scoped to users; file/folder ownership comes next.
                .securityMatcher("/api/users/**")
                // This chain uses explicit Bearer headers, not browser-supplied session cookies.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .build();
    }
}
