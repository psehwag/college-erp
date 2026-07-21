package com.erp.parent.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.parent.entity.Parent;
import com.erp.parent.service.ParentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    /** ADMIN only — list all parents paginated */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Parent>>> getAll(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        RoleGuard.requireAdmin(role);
        Page<Parent> data = parentService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.success("Parents fetched", data, data.getTotalElements()));
    }

    /** ADMIN only — create parent and auto-generate login */
    @PostMapping
    public ResponseEntity<ApiResponse<Parent>> create(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        Parent p = parentService.create(req);
        String username = parentService.getLoginUsername(p.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Parent created. Login: " + username + " | Default password: Password@123", p));
    }

    /** ADMIN — any parent. PARENT — own profile */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Parent>> getById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "PARENT");
        if ("PARENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Parent fetched", parentService.getById(id)));
    }

    /** PARENT — own profile shortcut */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Parent>> getMe(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "PARENT");
        Long id = parseLong(referenceId);
        if (id == null) return ResponseEntity.badRequest().body(ApiResponse.error("Reference ID missing"));
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", parentService.getById(id)));
    }

    /** ADMIN — any. PARENT — own */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Parent>> update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "PARENT");
        if ("PARENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Profile updated", parentService.update(id, req)));
    }

    /** ADMIN only — PERMANENTLY delete a parent (children are unlinked, not deleted) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        parentService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Parent permanently deleted (children unlinked, not deleted)", null));
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }
}
