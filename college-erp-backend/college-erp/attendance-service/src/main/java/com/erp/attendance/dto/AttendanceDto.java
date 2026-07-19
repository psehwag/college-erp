package com.erp.attendance.dto;

import com.erp.attendance.entity.Attendance;
import com.erp.attendance.entity.AttendanceSession;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AttendanceDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BulkMarkRequest {
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private LocalDate attendanceDate;
        private List<StudentAttendance> studentAttendances;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StudentAttendance {
        private Long studentId;
        private Attendance.AttendanceStatus status;
        private String remarks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FaceRecognitionMarkRequest {
        private Long studentId;
        private Long subjectId;
        private Long facultyId;
        private Long batchId;
        private String sessionToken;
        private Double confidenceScore;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StartSessionRequest {
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long studentId;
        private Long subjectId;
        private Long facultyId;
        private LocalDate attendanceDate;
        private LocalTime checkInTime;
        private Attendance.AttendanceStatus status;
        private Attendance.MarkedBy markedBy;
        private Double confidenceScore;
        private String remarks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AttendancePercentage {
        private Long studentId;
        private Long subjectId;
        private Long totalClasses;
        private Long presentClasses;
        private Double percentage;
        private Boolean isShortfall;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionResponse {
        private Long id;
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AttendanceSession.SessionStatus status;
        private String sessionToken;
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
