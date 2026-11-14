package com.mycloud.backend.common.exception;

import com.mycloud.backend.common.dto.ItemErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ItemExceptionHandler {

    @ExceptionHandler(ItemAccessDeniedException.class)
    public ResponseEntity<ItemErrorResponse> handleAccessDenied(ItemAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ItemErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ItemNameConflictException.class)
    public ResponseEntity<ItemErrorResponse> handleNameConflict(ItemNameConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ItemErrorResponse(exception.getMessage()));
    }
}
