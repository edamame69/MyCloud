package com.mycloud.backend.users.controller;

import com.mycloud.backend.users.dto.UserResponse;
import com.mycloud.backend.users.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        String subject = jwt.getSubject();
        UUID id = UUID.fromString(subject);
        return userService.getCurrentUser(id);
    }
}
