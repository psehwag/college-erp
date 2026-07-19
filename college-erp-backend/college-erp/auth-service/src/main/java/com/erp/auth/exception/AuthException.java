package com.erp.auth.exception;

import com.erp.auth.dto.AuthDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
