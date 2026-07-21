package com.erp.admin.controller;

import com.erp.admin.service.AdminService;
import com.erp.auth.dto.AuthDto;
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

    /** ADMIN only — full dashboard stats (all real, no dummy data) */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @RequestHeader("X-User-Role") String role) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data", adminService.getDashboard()));
    }

    /** ADMIN only — defaulter report for a subject (optionally scoped to one batch) */
    @GetMapping("/reports/defaulters/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<Long>>> getDefaulters(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long subjectId,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false, defaultValue = "75.0") Double threshold) {
        RoleGuard.requireAdmin(role);
        List<Long> result = batchId != null
                ? adminService.getDefaultersByBatch(subjectId, batchId, threshold)
                : adminService.getDefaulters(subjectId, threshold);
        return ResponseEntity.ok(ApiResponse.success("Defaulter report", result));
    }

    /**
     * ADMIN only — broadcast an announcement to one or more roles.
     * Only ACTIVE users of the selected roles receive it.
     */
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcast(
            @RequestHeader("X-User-Role") String role,
            @RequestBody AuthDto.BroadcastRequest req) {
        RoleGuard.requireAdmin(role);
        adminService.broadcastAnnouncement(req.getTitle(), req.getMessage(),
                req.getTargetRoles(), req.isSendEmail());
        return ResponseEntity.ok(ApiResponse.success("Announcement sent to active users", null));
    }
}
