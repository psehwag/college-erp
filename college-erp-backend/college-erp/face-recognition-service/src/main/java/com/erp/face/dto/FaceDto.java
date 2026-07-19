package com.erp.face.dto;

import lombok.*;

import java.util.List;

public class FaceDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EnrollmentResponse {
        private Long studentId;
        private int enrolledImages;
        private String message;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrainingRequest {
        private Long batchId;
        private List<Long> studentIds;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TrainingResponse {
        private Long batchId;
        private int trainedSamples;
        private int uniqueStudents;
        private String modelPath;
        private String message;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecognitionRequest {
        private Long batchId;
        private Long subjectId;
        private Long facultyId;
        private String sessionToken;
        // Base64-encoded frame image
        private String frameBase64;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecognitionResult {
        private boolean recognized;
        private Long studentId;
        private Double confidenceScore;
        private Double rawConfidence;
        private String message;
    }

    @Data @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(String msg, T data) {
            return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
        }
        public static <T> ApiResponse<T> error(String msg) {
            return ApiResponse.<T>builder().success(false).message(msg).build();
        }
    }
}
