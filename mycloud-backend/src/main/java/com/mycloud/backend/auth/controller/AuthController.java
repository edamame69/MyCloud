package com.mycloud.backend.auth.controller;

import com.mycloud.backend.auth.dto.AuthMessageResponse;
import com.mycloud.backend.auth.dto.LoginRequest;
import com.mycloud.backend.auth.dto.LoginResponse;
import com.mycloud.backend.auth.dto.RegisterRequest;
import com.mycloud.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.email(), request.firstName(), request.lastName(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthMessageResponse("Tạo tài khoản thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.email(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(response);
    }
}
