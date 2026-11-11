package com.mycloud.backend.folders.exception;

public class FolderNameConflictException extends RuntimeException {

    public FolderNameConflictException() {
        super("Tên thư mục đã tồn tại");
    }
}
