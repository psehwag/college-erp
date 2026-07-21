package com.erp.auth.controller;

import com.erp.auth.dto.AuthDto;
import com.erp.auth.entity.User;
import com.erp.auth.service.AuthService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── Public endpoints ──────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authService.login(req)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshTokenRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", authService.refreshToken(req)));
    }

    // ── Authenticated endpoints ───────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    /**
     * Any authenticated user can change their own password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AuthDto.ChangePasswordRequest req) {
        authService.changePassword(userId, req);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null));
    }

    /**
     * ADMIN only — reset any user's password without knowing old password.
     */
    @PostMapping("/admin/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody AuthDto.AdminResetPasswordRequest req) {
        RoleGuard.requireAdmin(role);
        authService.adminResetPassword(req);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully", null));
    }

    /**
     * ADMIN only — create a new admin user.
     * Student/Faculty/Parent accounts are created automatically
     * when those records are created.
     */
    @PostMapping("/admin/create-admin")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> createAdmin(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody AuthDto.CreateAdminRequest req) {
        RoleGuard.requireAdmin(role);
        User user = authService.createAdminUser(req);
        AuthDto.UserInfo info = new AuthDto.UserInfo(
                user.getId(), user.getUsername(), user.getName(), user.getEmail(),
                user.getRole(), null, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin user created", info));
    }

    /**
     * ADMIN only — activate or deactivate any user.
     */
    @PatchMapping("/admin/users/{userId}/active")
    public ResponseEntity<ApiResponse<Void>> setActive(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long userId,
            @RequestParam boolean active) {
        RoleGuard.requireAdmin(role);
        authService.setActive(userId, active);
        return ResponseEntity.ok(
                ApiResponse.success("User " + (active ? "activated" : "deactivated"), null));
    }

    /** ADMIN only — list all admin accounts */
    @GetMapping("/admin/admins")
    public ResponseEntity<ApiResponse<java.util.List<AuthDto.AdminSummary>>> listAdmins(
            @RequestHeader("X-User-Role") String role) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Admins fetched", authService.listAdmins()));
    }

    /** ADMIN only — edit another (or own) admin's name/email */
    @PutMapping("/admin/admins/{id}")
    public ResponseEntity<ApiResponse<AuthDto.AdminSummary>> updateAdmin(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestBody AuthDto.UpdateAdminRequest req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Admin updated", authService.updateAdmin(id, req)));
    }

    /** ADMIN only — delete an admin account (cannot delete self or last admin) */
    @DeleteMapping("/admin/admins/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long requesterId,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        authService.deleteAdmin(id, requesterId);
        return ResponseEntity.ok(ApiResponse.success("Admin deleted", null));
    }
}
