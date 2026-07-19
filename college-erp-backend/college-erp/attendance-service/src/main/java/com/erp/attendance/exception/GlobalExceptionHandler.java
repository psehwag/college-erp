package com.erp.attendance.exception;

import com.erp.attendance.dto.AttendanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AttendanceException.class)
    public ResponseEntity<AttendanceDto.ApiResponse<Void>> handleAttendance(AttendanceException ex) {
        return ResponseEntity.badRequest().body(AttendanceDto.ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AttendanceDto.ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AttendanceDto.ApiResponse.error("An unexpected error occurred"));
    }
}
