package com.mycloud.backend.common.exception;

public class ItemNameConflictException extends RuntimeException {

    public ItemNameConflictException() {
        super("Tên đã tồn tại trong thư mục này");
    }
}
