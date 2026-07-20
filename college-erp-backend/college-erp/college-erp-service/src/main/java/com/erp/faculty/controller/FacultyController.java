package com.erp.faculty.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.faculty.dto.FacultyDto;
import com.erp.faculty.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    /** ADMIN only */
    @PostMapping
    public ResponseEntity<ApiResponse<FacultyDto.Response>> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody FacultyDto.CreateRequest req) {
        RoleGuard.requireAdmin(role);
        FacultyDto.Response res = facultyService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Faculty created. Login: " + res.getLoginUsername()
                        + " | Default password: Password@123", res));
    }

    /** ADMIN, FACULTY */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto.Response>> getById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Faculty fetched", facultyService.getById(id)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<ApiResponse<FacultyDto.Response>> getByEmployeeId(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String empId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(ApiResponse.success("Faculty fetched", facultyService.getByEmployeeId(empId)));
    }

    /** ADMIN only for status changes, FACULTY can update own profile */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto.Response>> update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id,
            @Valid @RequestBody FacultyDto.UpdateRequest req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
            req.setStatus(null); // faculty cannot change own status
        }
        return ResponseEntity.ok(ApiResponse.success("Faculty updated", facultyService.update(id, req)));
    }

    /** ADMIN only */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        facultyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty deactivated", null));
    }

    /** ADMIN, FACULTY */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FacultyDto.Response>>> getAll(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        Page<FacultyDto.Response> data = facultyService.getAll(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Faculty list", data, data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<FacultyDto.Response>>> search(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(ApiResponse.success("Search results", facultyService.search(q, page, size)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<List<FacultyDto.Response>>> getByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(ApiResponse.success("Faculty by dept", facultyService.getByDepartment(deptId)));
    }

    /** ADMIN only — assign subject */
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<FacultyDto.AssignResponse>> assign(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody FacultyDto.AssignRequest req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject assigned", facultyService.assignSubject(req)));
    }

    /** ADMIN, FACULTY */
    @GetMapping("/{facultyId}/assignments")
    public ResponseEntity<ApiResponse<List<FacultyDto.AssignResponse>>> getAssignments(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long facultyId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(ApiResponse.success("Assignments", facultyService.getAssignments(facultyId)));
    }

    /** ADMIN only */
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long assignmentId) {
        RoleGuard.requireAdmin(role);
        facultyService.removeAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Assignment removed", null));
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }
}
