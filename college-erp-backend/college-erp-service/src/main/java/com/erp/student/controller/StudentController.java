package com.erp.student.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.student.dto.StudentDto;
import com.erp.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * ADMIN only — create a new student (auto-creates login credentials).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentDto.Response>> create(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody StudentDto.CreateRequest req) {
        RoleGuard.requireAdmin(role);
        StudentDto.Response res = studentService.createStudent(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Student created. Login: " + res.getLoginUsername()
                        + " | Default password: Password@123", res));
    }

    /**
     * ADMIN, FACULTY — view any student.
     * STUDENT — view own profile only.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDto.Response>> getById(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Student fetched", studentService.getById(id)));
    }

    @GetMapping("/enrollment/{en}")
    public ResponseEntity<ApiResponse<StudentDto.Response>> getByEnrollment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable String en) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(
                ApiResponse.success("Student fetched", studentService.getByEnrollment(en)));
    }

    /**
     * ADMIN — update any student.
     * STUDENT — update own profile only (limited fields handled by service).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentDto.Response>> update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long id,
            @Valid @RequestBody StudentDto.UpdateRequest req) {
        RoleGuard.requireAnyRole(role, "ADMIN", "STUDENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, id, parseLong(referenceId));
            // Students cannot change their own status
            req.setStatus(null);
        }
        return ResponseEntity.ok(
                ApiResponse.success("Student updated", studentService.update(id, req)));
    }

    /**
     * ADMIN only — PERMANENTLY delete a student and all dependent data
     * (attendance history, marks, face enrollment images, login account).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        studentService.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Student and all dependent data permanently deleted", null));
    }

    /**
     * ADMIN, FACULTY — list all students.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudentDto.Response>>> getAll(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        Page<StudentDto.Response> data = studentService.getAll(page, size, sort);
        return ResponseEntity.ok(
                ApiResponse.success("Students fetched", data, data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StudentDto.Response>>> search(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(
                ApiResponse.success("Search results", studentService.search(q, page, size)));
    }

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<ApiResponse<List<StudentDto.Summary>>> getByBatch(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long batchId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(
                ApiResponse.success("Students fetched", studentService.getByBatch(batchId)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<List<StudentDto.Summary>>> getByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY");
        return ResponseEntity.ok(
                ApiResponse.success("Students fetched", studentService.getByDepartment(deptId)));
    }

    /**
     * PARENT — view their own children.
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<StudentDto.Summary>>> getByParent(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long parentId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "PARENT");
        if ("PARENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, parentId, parseLong(referenceId));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Students fetched", studentService.getByParent(parentId)));
    }

    /**
     * ADMIN only — mark face enrolled after AI enrollment.
     */
    @PatchMapping("/{id}/face-enrolled")
    public ResponseEntity<ApiResponse<Void>> markFaceEnrolled(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        studentService.markFaceEnrolled(id);
        return ResponseEntity.ok(ApiResponse.success("Face enrollment updated", null));
    }

    private Long parseLong(String val) {
        if (val == null || val.isEmpty()) return null;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return null; }
    }
}
