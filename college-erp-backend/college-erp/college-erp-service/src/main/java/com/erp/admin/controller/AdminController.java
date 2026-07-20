package com.erp.admin.controller;

import com.erp.admin.service.AdminService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** ADMIN only — full dashboard stats */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader("X-User-Role") String role) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data", adminService.getDashboard()));
    }

    /** ADMIN only — defaulter report for a subject */
    @GetMapping("/reports/defaulters/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<Long>>> getDefaulters(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long subjectId,
            @RequestParam(required = false, defaultValue = "75.0") Double threshold) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(
                ApiResponse.success("Defaulter report", adminService.getDefaulters(subjectId, threshold)));
    }

    /** ADMIN only — broadcast announcement via Kafka */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcast(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "ALL") String targetRole) {
        RoleGuard.requireAdmin(role);
        adminService.broadcastAnnouncement(title, message, targetRole);
        return ResponseEntity.ok(ApiResponse.success("Announcement broadcast sent", null));
    }
}
