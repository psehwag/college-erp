package com.erp.admin.service;

import com.erp.admin.config.*;
import com.erp.admin.dto.AdminDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final StudentFeignClient studentClient;
    private final FacultyFeignClient facultyClient;
    private final AttendanceFeignClient attendanceClient;
    private final CourseFeignClient courseClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // ── Admin Dashboard ───────────────────────────────────────────────────

    public AdminDto.DashboardStats getAdminDashboard() {
        log.info("Building admin dashboard");

        // Fetch departments
        List<Map<String, Object>> departments = safeCall(() -> courseClient.getAllDepartments(), List.of());

        // Fetch students and faculty counts
        Map<String, Object> studentsPage = safeCall(
                () -> studentClient.getAllStudents(0, 1, "firstName"), Map.of());
        Long totalStudents = extractLong(studentsPage, "total", 0L);

        Map<String, Object> facultyPage = safeCall(
                () -> facultyClient.getAllFaculty(0, 1, "firstName"), Map.of());
        Long totalFaculty = extractLong(facultyPage, "total", 0L);

        // Department stats
        List<AdminDto.DepartmentStat> deptStats = departments.stream().map(dept -> {
            Long deptId = extractLong(dept, "id", 0L);
            String deptName = (String) dept.getOrDefault("name", "Unknown");

            List<Map<String, Object>> deptStudents = safeCall(
                    () -> studentClient.getStudentsByDepartment(deptId), List.of());
            List<Map<String, Object>> deptFaculty = safeCall(
                    () -> facultyClient.getFacultyByDepartment(deptId), List.of());

            return AdminDto.DepartmentStat.builder()
                    .departmentId(deptId)
                    .departmentName(deptName)
                    .studentCount((long) deptStudents.size())
                    .facultyCount((long) deptFaculty.size())
                    .avgAttendance(0.0) // computed separately per subject
                    .build();
        }).collect(Collectors.toList());

        return AdminDto.DashboardStats.builder()
                .totalStudents(totalStudents)
                .totalFaculty(totalFaculty)
                .totalDepartments((long) departments.size())
                .totalCourses(0L)   // populated by course-service
                .todayPresent(0L)
                .todayAbsent(0L)
                .todayAttendancePercentage(0.0)
                .attendanceTrend(List.of())
                .departmentStats(deptStats)
                .pendingFaceEnrollments(0L)
                .defaulterCount(0L)
                .build();
    }

    // ── Faculty Dashboard ─────────────────────────────────────────────────

    public AdminDto.FacultyDashboard getFacultyDashboard(Long facultyId) {
        return AdminDto.FacultyDashboard.builder()
                .facultyId(facultyId)
                .facultyName("Faculty #" + facultyId)
                .todayClasses(List.of())
                .totalStudentsAssigned(0L)
                .avgAttendanceRate(0.0)
                .pendingMarksUploads(0L)
                .build();
    }

    // ── Student Dashboard ─────────────────────────────────────────────────

    public AdminDto.StudentDashboard getStudentDashboard(Long studentId) {
        return AdminDto.StudentDashboard.builder()
                .studentId(studentId)
                .studentName("Student #" + studentId)
                .enrollmentNumber("")
                .currentSemester(1)
                .overallAttendancePercentage(0.0)
                .subjectAttendance(List.of())
                .hasAttendanceShortfall(false)
                .semesterPercentage(0.0)
                .recentMarks(List.of())
                .upcomingExams(List.of())
                .build();
    }

    // ── Defaulter Report ──────────────────────────────────────────────────

    public List<AdminDto.DefaulterReport> getDefaulterReport(Long subjectId, Double threshold) {
        List<Long> defaulterIds = safeCall(
                () -> attendanceClient.getDefaulters(subjectId, threshold), List.of());

        return defaulterIds.stream().map(studentId -> {
            Map<String, Object> pct = safeCall(
                    () -> attendanceClient.getAttendancePercentage(studentId, subjectId), Map.of());

            return AdminDto.DefaulterReport.builder()
                    .studentId(studentId)
                    .subjectId(subjectId)
                    .attendancePercentage(extractDouble(pct, "percentage", 0.0))
                    .totalClasses(extractLong(pct, "totalClasses", 0L))
                    .presentClasses(extractLong(pct, "presentClasses", 0L))
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Broadcast Announcement ────────────────────────────────────────────

    public void broadcastAnnouncement(String title, String message, String targetRole) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "GENERAL_ANNOUNCEMENT");
        event.put("title", title);
        event.put("message", message);
        event.put("recipientRole", targetRole);
        event.put("recipientId", -1L); // broadcast marker
        event.put("sendEmail", false);

        kafkaTemplate.send("general-events", event);
        log.info("Broadcast announcement sent: {} -> {}", title, targetRole);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private <T> T safeCall(java.util.function.Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Feign call failed: {}", e.getMessage());
            return fallback;
        }
    }

    private Long extractLong(Map<String, Object> map, String key, Long fallback) {
        Object val = map.get(key);
        if (val == null) return fallback;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return fallback; }
    }

    private Double extractDouble(Map<String, Object> map, String key, Double fallback) {
        Object val = map.get(key);
        if (val == null) return fallback;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return fallback; }
    }
}
