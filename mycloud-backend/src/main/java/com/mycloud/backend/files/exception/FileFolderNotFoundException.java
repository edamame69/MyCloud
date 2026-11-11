package com.mycloud.backend.files.exception;

public class FileFolderNotFoundException extends RuntimeException {

    public FileFolderNotFoundException() {
        super("Thư mục chứa tệp không tồn tại");
    }
}
