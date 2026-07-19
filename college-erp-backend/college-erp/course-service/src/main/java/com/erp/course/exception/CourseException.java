package com.erp.course.exception;

import com.erp.course.dto.CourseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

public class CourseException extends RuntimeException {
    public CourseException(String message) { super(message); }
}

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(CourseException.class)
    public ResponseEntity<CourseDto.ApiResponse<Void>> handle(CourseException ex) {
        return ResponseEntity.badRequest().body(CourseDto.ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CourseDto.ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(CourseDto.ApiResponse.error(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CourseDto.ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Course service error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CourseDto.ApiResponse.error("Unexpected error"));
    }
}
