package com.erp.marks.exception;

import com.erp.marks.dto.MarksDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

public class MarksException extends RuntimeException {
    public MarksException(String message) { super(message); }
}

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(MarksException.class)
    public ResponseEntity<MarksDto.ApiResponse<Void>> handleMarks(MarksException ex) {
        return ResponseEntity.badRequest().body(MarksDto.ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MarksDto.ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(MarksDto.ApiResponse.error(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MarksDto.ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Marks service error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MarksDto.ApiResponse.error("An unexpected error occurred"));
    }
}
