package com.mycloud.backend.common.exception;

public class ItemAccessDeniedException extends RuntimeException {
    public ItemAccessDeniedException() {
        super("Bạn không có quyền truy cập");
    }
}
