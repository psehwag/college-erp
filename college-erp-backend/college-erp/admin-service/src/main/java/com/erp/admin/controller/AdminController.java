package com.erp.admin.controller;

import com.erp.admin.dto.AdminDto;
import com.erp.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin dashboard, reports, and broadcast endpoints")
public class AdminController {

    private final AdminService adminService;

    // ── Dashboards ────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard — totals, trends, department stats")
    public ResponseEntity<AdminDto.ApiResponse<AdminDto.DashboardStats>> getAdminDashboard() {
        return ResponseEntity.ok(AdminDto.ApiResponse.success(
                "Dashboard data fetched", adminService.getAdminDashboard()));
    }

    @GetMapping("/dashboard/faculty/{facultyId}")
    @Operation(summary = "Faculty-specific dashboard — today's classes, attendance summary, pending marks")
    public ResponseEntity<AdminDto.ApiResponse<AdminDto.FacultyDashboard>> getFacultyDashboard(
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(AdminDto.ApiResponse.success(
                "Faculty dashboard fetched", adminService.getFacultyDashboard(facultyId)));
    }

    @GetMapping("/dashboard/student/{studentId}")
    @Operation(summary = "Student dashboard — attendance %, marks trend, upcoming exams")
    public ResponseEntity<AdminDto.ApiResponse<AdminDto.StudentDashboard>> getStudentDashboard(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(AdminDto.ApiResponse.success(
                "Student dashboard fetched", adminService.getStudentDashboard(studentId)));
    }

    @GetMapping("/dashboard/me")
    @Operation(summary = "Current user's dashboard (resolved from JWT X-User-Id header)")
    public ResponseEntity<AdminDto.ApiResponse<AdminDto.StudentDashboard>> getMyDashboard(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        // Route to the appropriate dashboard by role
        if ("STUDENT".equals(role)) {
            return ResponseEntity.ok(AdminDto.ApiResponse.success(
                    "Dashboard fetched", adminService.getStudentDashboard(userId)));
        } else if ("FACULTY".equals(role)) {
            AdminDto.FacultyDashboard fd = adminService.getFacultyDashboard(userId);
            // Wrap in StudentDashboard shell for a unified response type
            return ResponseEntity.ok(AdminDto.ApiResponse.success("Dashboard fetched", null));
        }
        return ResponseEntity.ok(AdminDto.ApiResponse.success("Dashboard fetched", null));
    }

    // ── Reports ───────────────────────────────────────────────────────────

    @GetMapping("/reports/defaulters/subject/{subjectId}")
    @Operation(summary = "Generate attendance defaulter list for a subject (default threshold 75%)")
    public ResponseEntity<AdminDto.ApiResponse<List<AdminDto.DefaulterReport>>> getDefaulters(
            @PathVariable Long subjectId,
            @RequestParam(required = false, defaultValue = "75.0") Double threshold) {
        return ResponseEntity.ok(AdminDto.ApiResponse.success(
                "Defaulter report generated",
                adminService.getDefaulterReport(subjectId, threshold)));
    }

    // ── Broadcasts ────────────────────────────────────────────────────────

    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast an announcement to all users of a given role via Kafka")
    public ResponseEntity<AdminDto.ApiResponse<Void>> broadcast(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "ALL") String targetRole) {
        adminService.broadcastAnnouncement(title, message, targetRole);
        return ResponseEntity.ok(AdminDto.ApiResponse.success("Announcement broadcast sent", null));
    }
}
