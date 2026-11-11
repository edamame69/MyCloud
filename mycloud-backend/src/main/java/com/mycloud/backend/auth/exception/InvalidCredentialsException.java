package com.mycloud.backend.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email hoặc mật khẩu không đúng");
    }
}
