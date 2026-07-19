package com.erp.student.exception;

import com.erp.student.dto.StudentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

public class StudentException extends RuntimeException {
    public StudentException(String message) { super(message); }
}
