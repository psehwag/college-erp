package com.erp.faculty.dto;

import com.erp.faculty.entity.Faculty;
import com.erp.faculty.entity.FacultySubjectAssignment;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FacultyDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @Email @NotBlank private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Faculty.Gender gender;
        private String address;
        @NotNull private Long departmentId;
        private String designation;
        private String qualification;
        private String specialization;
        private LocalDate joiningDate;
        private Integer experienceYears;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String address;
        private String designation;
        private String qualification;
        private String specialization;
        private Integer experienceYears;
        private Faculty.FacultyStatus status;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String employeeId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Faculty.Gender gender;
        private String address;
        private String photoUrl;
        private Long departmentId;
        private String designation;
        private String qualification;
        private String specialization;
        private LocalDate joiningDate;
        private Integer experienceYears;
        private Faculty.FacultyStatus status;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignSubjectRequest {
        @NotNull private Long facultyId;
        @NotNull private Long subjectId;
        @NotNull private Long batchId;
        @NotBlank private String academicYear;
        @NotNull private Integer semester;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignmentResponse {
        private Long id;
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private String academicYear;
        private Integer semester;
        private Boolean isActive;
        private LocalDateTime assignedAt;
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
