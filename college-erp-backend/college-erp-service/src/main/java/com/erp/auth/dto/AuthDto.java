package com.erp.auth.dto;

import com.erp.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDto {

    // ── Login ─────────────────────────────────────────────────────────────

    public static class LoginRequest {
        @NotBlank(message = "Username or email is required")
        private String usernameOrEmail;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ── Login response ────────────────────────────────────────────────────

    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserInfo user;

        public LoginResponse() {}

        public LoginResponse(String accessToken, String refreshToken,
                             String tokenType, Long expiresIn, UserInfo user) {
            this.accessToken  = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType    = tokenType;
            this.expiresIn    = expiresIn;
            this.user         = user;
        }

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }
    }

    // ── User info ─────────────────────────────────────────────────────────

    public static class UserInfo {
        private Long id;
        private String username;
        private String name;
        private String email;
        private User.Role role;
        private Long referenceId;
        private Boolean mustChangePassword;

        public UserInfo() {}

        public UserInfo(Long id, String username, String name, String email,
                        User.Role role, Long referenceId, Boolean mustChangePassword) {
            this.id                 = id;
            this.username           = username;
            this.name               = name;
            this.email              = email;
            this.role               = role;
            this.referenceId        = referenceId;
            this.mustChangePassword = mustChangePassword;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
        public Long getReferenceId() { return referenceId; }
        public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
        public Boolean getMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    }

    // ── Refresh token ─────────────────────────────────────────────────────

    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;

        public RefreshTokenRequest() {}
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    // ── Change password (own) ─────────────────────────────────────────────

    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;

        public ChangePasswordRequest() {}
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // ── Admin reset any user's password ──────────────────────────────────

    public static class AdminResetPasswordRequest {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPassword;

        public AdminResetPasswordRequest() {}
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // ── Create admin user ─────────────────────────────────────────────────

    public static class CreateAdminRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        private String name;

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        public CreateAdminRequest() {}
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ── Admin self-management (list/edit/delete admin accounts) ─────────────

    public static class UpdateAdminRequest {
        private String name;
        @Email
        private String email;

        public UpdateAdminRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class AdminSummary {
        private Long id;
        private String username;
        private String name;
        private String email;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;

        public AdminSummary() {}
        public AdminSummary(Long id, String username, String name, String email,
                            Boolean isActive, java.time.LocalDateTime createdAt) {
            this.id = id; this.username = username; this.name = name;
            this.email = email; this.isActive = isActive; this.createdAt = createdAt;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ── Broadcast (multi-role, excludes inactive users) ──────────────────────

    public static class BroadcastRequest {
        @NotBlank private String title;
        @NotBlank private String message;
        private java.util.List<String> targetRoles; // e.g. ["STUDENT","FACULTY","PARENT"]
        private boolean sendEmail = false;

        public BroadcastRequest() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public java.util.List<String> getTargetRoles() { return targetRoles; }
        public void setTargetRoles(java.util.List<String> targetRoles) { this.targetRoles = targetRoles; }
        public boolean isSendEmail() { return sendEmail; }
        public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }
    }
}
