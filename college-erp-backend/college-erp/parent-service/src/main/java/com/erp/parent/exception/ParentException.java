package com.erp.parent.exception;

import com.erp.parent.dto.ParentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

public class ParentException extends RuntimeException {
    public ParentException(String message) { super(message); }
}

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {
    @ExceptionHandler(ParentException.class)
    public ResponseEntity<ParentDto.ApiResponse<Void>> handle(ParentException ex) {
        return ResponseEntity.badRequest().body(ParentDto.ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ParentDto.ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Parent service error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ParentDto.ApiResponse.error("Unexpected error"));
    }
}
