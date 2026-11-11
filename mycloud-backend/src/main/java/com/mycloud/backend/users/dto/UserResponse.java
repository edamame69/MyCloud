package com.mycloud.backend.users.dto;

import com.mycloud.backend.users.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }
}
