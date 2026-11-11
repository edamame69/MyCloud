package com.mycloud.backend.files.exception;

public class FileNameConflictException extends RuntimeException {
    public FileNameConflictException() {
        super("Tên tệp đã tồn tại");
    }
}
