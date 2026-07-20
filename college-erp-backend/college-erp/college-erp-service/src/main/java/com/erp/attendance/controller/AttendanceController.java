package com.erp.attendance.controller;

import com.erp.attendance.dto.AttendanceDto;
import com.erp.attendance.service.AttendanceService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** ADMIN, FACULTY — bulk manual marking */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<AttendanceDto.Response>>> markBulk(
            @RequestHeader("X-User-Role") String role,
            @RequestBody AttendanceDto.BulkMarkRequest req) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked",
                attendanceService.markBulk(req)));
    }

    /** ADMIN, FACULTY — face recognition marking */
    @PostMapping("/face")
    public ResponseEntity<ApiResponse<AttendanceDto.Response>> markByFace(
            @RequestHeader("X-User-Role") String role,
            @RequestBody AttendanceDto.FaceMarkRequest req) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked via face recognition",
                attendanceService.markByFace(req)));
    }

    /** ADMIN, FACULTY — start face recognition session */
    @PostMapping("/session/start")
    public ResponseEntity<ApiResponse<AttendanceDto.SessionResponse>> startSession(
            @RequestHeader("X-User-Role") String role,
            @RequestBody AttendanceDto.StartSessionRequest req) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Session started",
                attendanceService.startSession(req)));
    }

    /** ADMIN, FACULTY — end session */
    @PatchMapping("/session/{sessionId}/end")
    public ResponseEntity<ApiResponse<AttendanceDto.SessionResponse>> endSession(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long sessionId) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Session ended",
                attendanceService.endSession(sessionId)));
    }

    /** All authenticated — get percentage (student sees own, faculty/admin see any) */
    @GetMapping("/student/{studentId}/subject/{subjectId}/percentage")
    public ResponseEntity<ApiResponse<AttendanceDto.PercentageResponse>> getPercentage(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long studentId,
            @PathVariable Long subjectId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, studentId, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Attendance percentage",
                attendanceService.getPercentage(studentId, subjectId)));
    }

    /** ADMIN, FACULTY — attendance by subject and date */
    @GetMapping("/subject/{subjectId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceDto.Response>>> getByDate(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long subjectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Attendance fetched",
                attendanceService.getByDate(subjectId, date)));
    }

    /** STUDENT — own attendance history */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<AttendanceDto.Response>>> getByStudent(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long studentId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, studentId, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Attendance history",
                attendanceService.getByStudent(studentId)));
    }

    /** ADMIN, FACULTY — defaulter list */
    @GetMapping("/subject/{subjectId}/defaulters")
    public ResponseEntity<ApiResponse<List<Long>>> getDefaulters(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long subjectId,
            @RequestParam(required = false) Double threshold) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Defaulters fetched",
                attendanceService.getDefaulters(subjectId, threshold)));
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }
}
