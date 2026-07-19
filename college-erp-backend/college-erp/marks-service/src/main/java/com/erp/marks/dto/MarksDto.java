package com.erp.marks.dto;

import com.erp.marks.entity.Marks;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class MarksDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertRequest {
        @NotNull private Long studentId;
        @NotNull private Long subjectId;
        @NotNull private Long facultyId;
        private Long batchId;
        @NotNull private Integer semester;
        @NotNull private Marks.ExamType examType;
        @NotNull @DecimalMin("0") private Double marksObtained;
        @NotNull @DecimalMin("1") private Double maxMarks;
        @NotBlank private String academicYear;
        private String remarks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BulkUpsertRequest {
        @NotNull private Long subjectId;
        @NotNull private Long facultyId;
        @NotNull private Long batchId;
        @NotNull private Integer semester;
        @NotNull private Marks.ExamType examType;
        @NotNull @DecimalMin("1") private Double maxMarks;
        @NotBlank private String academicYear;
        @NotEmpty private List<StudentMark> studentMarks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StudentMark {
        private Long studentId;
        private Double marksObtained;
        private String remarks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long studentId;
        private Long subjectId;
        private Long facultyId;
        private Long batchId;
        private Integer semester;
        private Marks.ExamType examType;
        private Double marksObtained;
        private Double maxMarks;
        private Double percentage;
        private Marks.Grade grade;
        private String academicYear;
        private String remarks;
        private LocalDateTime updatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentSummary {
        private Long studentId;
        private Integer semester;
        private Double totalObtained;
        private Double totalMax;
        private Double overallPercentage;
        private Marks.Grade overallGrade;
        private List<Response> subjectMarks;
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
