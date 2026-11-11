package com.mycloud.backend.files.exception;

public class FileMetadataNotFoundException extends RuntimeException {

    public FileMetadataNotFoundException() {
        super("Tệp không tồn tại");
    }
}
