package com.erp.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ──────────────────────────────────────────────────────

    public User() {}

    public User(Long id, String username, String email, String password,
                Role role, Long referenceId, Boolean isActive,
                Boolean isEmailVerified, Boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.referenceId = referenceId;
        this.isActive = isActive;
        this.isEmailVerified = isEmailVerified;
        this.mustChangePassword = mustChangePassword;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum Role {
        ADMIN, FACULTY, STUDENT, PARENT
    }
}
