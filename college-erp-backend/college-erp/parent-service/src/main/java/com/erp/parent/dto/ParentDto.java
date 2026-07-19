package com.erp.parent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

public class ParentDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        private String phone;
        private String alternatePhone;
        private String address;
        private String occupation;
        private String relationToStudent;
        private boolean receiveSms;
        private boolean receiveEmail;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private String alternatePhone;
        private String address;
        private String occupation;
        private String relationToStudent;
        private Boolean isActive;
        private Boolean receiveSms;
        private Boolean receiveEmail;
        private LocalDateTime createdAt;
    }

    // Student view for parent portal - aggregates from other services
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentView {
        private Long studentId;
        private String studentName;
        private String enrollmentNumber;
        private Integer currentSemester;
        private Object attendanceSummary;  // populated via Feign
        private Object latestMarks;        // populated via Feign
    }

    @Data @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        public static <T> ApiResponse<T> success(String m, T d) {
            return ApiResponse.<T>builder().success(true).message(m).data(d).build();
        }
        public static <T> ApiResponse<T> error(String m) {
            return ApiResponse.<T>builder().success(false).message(m).build();
        }
    }
}
