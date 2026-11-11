package com.mycloud.backend.users.exception;

public class InvalidSessionException extends RuntimeException {
    public static final String MESSAGE = "Phiên đăng nhập không hợp lệ hoặc đã hết hạn";

    public InvalidSessionException() {
        super(MESSAGE);
    }
}
