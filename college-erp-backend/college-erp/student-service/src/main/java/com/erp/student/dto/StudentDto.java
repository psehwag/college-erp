package com.erp.student.dto;

import com.erp.student.entity.Student;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;
        @NotNull private Long departmentId;
        @NotNull private Long courseId;
        private Long batchId;
        @NotNull @Min(1) @Max(10) private Integer currentSemester;
        @NotNull private Integer admissionYear;
        private Long parentId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;
        private Long batchId;
        private Integer currentSemester;
        private Long parentId;
        private Student.StudentStatus status;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String enrollmentNumber;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Student.Gender gender;
        private String address;
        private String photoUrl;
        private Long departmentId;
        private Long courseId;
        private Long batchId;
        private Integer currentSemester;
        private Integer admissionYear;
        private Long parentId;
        private Student.StudentStatus status;
        private Boolean faceEnrolled;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Summary {
        private Long id;
        private String enrollmentNumber;
        private String fullName;
        private String email;
        private Long departmentId;
        private Integer currentSemester;
        private Student.StudentStatus status;
    }

    @Data @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private Long total;

        public static <T> ApiResponse<T> success(String msg, T data) {
            return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
        }
        public static <T> ApiResponse<T> error(String msg) {
            return ApiResponse.<T>builder().success(false).message(msg).build();
        }
    }
}
