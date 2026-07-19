package com.erp.course.dto;

import com.erp.course.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDto {

    // ── Department ────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeptCreateRequest {
        @NotBlank private String name;
        @NotBlank @Size(max=10) private String code;
        private String description;
        private Long headFacultyId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeptResponse {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Long headFacultyId;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // ── Course ────────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CourseCreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
        @NotNull private Long departmentId;
        @NotNull @Min(1) @Max(20) private Integer totalSemesters;
        private Integer durationYears;
        private Course.CourseType type;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CourseResponse {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Long departmentId;
        private Integer totalSemesters;
        private Integer durationYears;
        private Course.CourseType type;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // ── Subject ───────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubjectCreateRequest {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
        @NotNull private Long courseId;
        @NotNull private Long departmentId;
        @NotNull @Min(1) private Integer semester;
        @NotNull @Min(1) private Integer credits;
        private Integer totalLectures;
        private Subject.SubjectType type;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubjectResponse {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Long courseId;
        private Long departmentId;
        private Integer semester;
        private Integer credits;
        private Integer totalLectures;
        private Subject.SubjectType type;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // ── Batch ─────────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BatchCreateRequest {
        @NotBlank private String name;
        @NotNull private Long courseId;
        @NotNull private Long departmentId;
        @NotBlank private String academicYear;
        private Integer currentSemester;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxStrength;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BatchResponse {
        private Long id;
        private String name;
        private Long courseId;
        private Long departmentId;
        private String academicYear;
        private Integer currentSemester;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer maxStrength;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // ── Common ────────────────────────────────────────────────────────────

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
