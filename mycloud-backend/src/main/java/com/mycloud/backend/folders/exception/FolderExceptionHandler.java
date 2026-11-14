package com.mycloud.backend.folders.exception;

import com.mycloud.backend.folders.dto.FolderErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FolderExceptionHandler {

    @ExceptionHandler(FolderParentNotFoundException.class)
    public ResponseEntity<FolderErrorResponse> handleParentNotFound(FolderParentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new FolderErrorResponse(exception.getMessage()));
    }
}
