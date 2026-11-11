package com.mycloud.backend.auth.exception;

import com.mycloud.backend.auth.controller.AuthController;
import com.mycloud.backend.auth.dto.AuthMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<AuthMessageResponse> handleDuplicateEmail(EmailAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<AuthMessageResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new AuthMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<AuthMessageResponse> handleInvalidRequest(HttpServletRequest request) {
        // Do not expose rejected values: they may contain the plaintext password.
        return ResponseEntity.badRequest()
                .body(new AuthMessageResponse(request.getRequestURI().endsWith("/login")
                        ? "Dữ liệu đăng nhập không hợp lệ" : "Dữ liệu đăng ký không hợp lệ"));
    }


}
