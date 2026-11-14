package com.mycloud.backend.users.exception;

import com.mycloud.backend.auth.dto.AuthMessageResponse;
import com.mycloud.backend.users.controller.UserController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {
    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<AuthMessageResponse> handleInvalidSession(InvalidSessionException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new AuthMessageResponse(exception.getMessage()));
    }
}
