package com.erp.auth.controller;

import com.erp.auth.dto.AuthDto;
import com.erp.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints for login, register, token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/email and password")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(AuthDto.ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.LoginResponse>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        AuthDto.LoginResponse response = authService.register(request);
        return ResponseEntity.ok(AuthDto.ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthDto.ApiResponse<AuthDto.LoginResponse>> refreshToken(
            @Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        AuthDto.LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(AuthDto.ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public ResponseEntity<AuthDto.ApiResponse<Void>> logout(
            @RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(AuthDto.ApiResponse.success("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<AuthDto.ApiResponse<Void>> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(AuthDto.ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
