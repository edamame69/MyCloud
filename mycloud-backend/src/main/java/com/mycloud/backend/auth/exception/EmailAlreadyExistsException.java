package com.mycloud.backend.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Email đã tồn tại");
    }
}
