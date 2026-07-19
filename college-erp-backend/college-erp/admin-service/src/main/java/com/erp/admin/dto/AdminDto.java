package com.erp.admin.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

public class AdminDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DashboardStats {
        // Totals
        private Long totalStudents;
        private Long totalFaculty;
        private Long totalDepartments;
        private Long totalCourses;
        // Today
        private Long todayPresent;
        private Long todayAbsent;
        private Double todayAttendancePercentage;
        // Trends (last 7 days)
        private List<AttendanceTrend> attendanceTrend;
        // Dept breakdown
        private List<DepartmentStat> departmentStats;
        // Alerts
        private Long pendingFaceEnrollments;
        private Long defaulterCount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AttendanceTrend {
        private String date;
        private Long present;
        private Long absent;
        private Double percentage;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DepartmentStat {
        private Long departmentId;
        private String departmentName;
        private Long studentCount;
        private Long facultyCount;
        private Double avgAttendance;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DefaulterReport {
        private Long studentId;
        private String enrollmentNumber;
        private String studentName;
        private Long subjectId;
        private String subjectName;
        private Double attendancePercentage;
        private Long totalClasses;
        private Long presentClasses;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FacultyDashboard {
        private Long facultyId;
        private String facultyName;
        private List<Map<String, Object>> todayClasses;
        private Long totalStudentsAssigned;
        private Double avgAttendanceRate;
        private Long pendingMarksUploads;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentDashboard {
        private Long studentId;
        private String studentName;
        private String enrollmentNumber;
        private Integer currentSemester;
        // Attendance
        private Double overallAttendancePercentage;
        private List<Map<String, Object>> subjectAttendance;
        private Boolean hasAttendanceShortfall;
        // Marks
        private Double semesterPercentage;
        private List<Map<String, Object>> recentMarks;
        // Upcoming exams
        private List<Map<String, Object>> upcomingExams;
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
