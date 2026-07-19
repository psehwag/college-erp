package com.erp.face.exception;

import com.erp.face.dto.FaceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

public class FaceRecognitionException extends RuntimeException {
    public FaceRecognitionException(String message) { super(message); }
}

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(FaceRecognitionException.class)
    public ResponseEntity<FaceDto.ApiResponse<Void>> handle(FaceRecognitionException ex) {
        return ResponseEntity.badRequest().body(FaceDto.ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FaceDto.ApiResponse<Void>> handleFileSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(FaceDto.ApiResponse.error("File too large. Max 10MB per photo."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FaceDto.ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Face recognition error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FaceDto.ApiResponse.error("Face recognition error: " + ex.getMessage()));
    }
}
