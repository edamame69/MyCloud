package com.mycloud.backend.auth.dto;

public record LoginResponse(String message, String accessToken, String tokenType, long expiresIn) {
    @Override
    public String toString() {
        return "LoginResponse[redacted]";
    }
}
