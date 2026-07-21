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

    /** ADMIN — full edit including status. FACULTY — own profile, cannot change own status/department. */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacultyDto.Response>> update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id,
            @Valid @RequestBody FacultyDto.UpdateRequest req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
            req.setStatus(null);
            req.setDepartmentId(null);
        }
        return ResponseEntity.ok(ApiResponse.success("Faculty updated", facultyService.update(id, req)));
    }

    /** ADMIN only — PERMANENTLY delete faculty and their teaching assignments */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        facultyService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Faculty and their assignments permanently deleted", null));
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

    /**
     * ADMIN or FACULTY (self-service) — create a teaching assignment.
     * A faculty can hold many assignment rows, each pinning one
     * subject + batch + semester + academic year, so a single faculty
     * can be spread across many departments/courses/semesters/subjects/batches.
     */
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<FacultyDto.AssignResponse>> assign(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @Valid @RequestBody FacultyDto.AssignRequest req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, req.getFacultyId(), parseLong(referenceId));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject assigned", facultyService.assignSubject(req)));
    }

    /** ADMIN, FACULTY (self-service) — edit assignment description */
    @PutMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<FacultyDto.AssignResponse>> updateAssignment(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long assignmentId,
            @RequestBody FacultyDto.AssignUpdateRequest req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            Long owner = facultyService.getFacultyIdOwningAssignment(assignmentId);
            RoleGuard.requireOwnerOrAdmin(role, owner, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Assignment updated",
                facultyService.updateAssignment(assignmentId, req)));
    }

    /** ADMIN, FACULTY */
    @GetMapping("/{facultyId}/assignments")
    public ResponseEntity<ApiResponse<List<FacultyDto.AssignResponse>>> getAssignments(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long facultyId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(ApiResponse.success("Assignments", facultyService.getAssignments(facultyId)));
    }

    /** ADMIN, FACULTY (self-service) */
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long assignmentId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        if ("FACULTY".equals(role)) {
            Long owner = facultyService.getFacultyIdOwningAssignment(assignmentId);
            RoleGuard.requireOwnerOrAdmin(role, owner, parseLong(referenceId));
        }
        facultyService.removeAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.success("Assignment removed", null));
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }
}
