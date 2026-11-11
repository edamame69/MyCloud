package com.mycloud.backend.folders.exception;

public class FolderParentNotFoundException extends RuntimeException {

    public FolderParentNotFoundException() {
        super("Thư mục cha không tồn tại");
    }
}
