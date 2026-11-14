package com.mycloud.backend.files.exception;

import com.mycloud.backend.files.dto.FileErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(FileMetadataNotFoundException.class)
    public ResponseEntity<FileErrorResponse> handleNotFound(FileMetadataNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new FileErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(FileFolderNotFoundException.class)
    public ResponseEntity<FileErrorResponse> handleFolderNotFound(FileFolderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new FileErrorResponse(exception.getMessage()));
    }
}
