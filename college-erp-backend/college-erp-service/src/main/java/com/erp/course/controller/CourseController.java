package com.erp.course.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.course.entity.*;
import com.erp.course.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // ── Departments ───────────────────────────────────────────────────────

    @PostMapping("/api/departments")
    public ResponseEntity<ApiResponse<Department>> createDept(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created", courseService.createDepartment(req)));
    }

    @GetMapping("/api/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepts(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        // Only admin may request inactive departments too
        boolean effectiveIncludeInactive = includeInactive && "ADMIN".equals(role);
        return ResponseEntity.ok(ApiResponse.success("Departments",
                courseService.getAllDepartments(effectiveIncludeInactive)));
    }

    @GetMapping("/api/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> getDeptById(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Department", courseService.getDepartmentById(id)));
    }

    @PutMapping("/api/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Department updated", courseService.updateDepartment(id, req)));
    }

    /** ADMIN only — activate/deactivate a department */
    @PatchMapping("/api/departments/{id}/status")
    public ResponseEntity<ApiResponse<Department>> setDeptActive(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam boolean active) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(
                active ? "Department activated" : "Department deactivated",
                courseService.setDepartmentActive(id, active)));
    }

    /** ADMIN only — PERMANENTLY delete a department and everything under it */
    @DeleteMapping("/api/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        courseService.deleteDepartmentCascade(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Department and all its courses, subjects, batches, students and faculty permanently deleted", null));
    }

    @GetMapping("/api/departments/{deptId}/courses")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        boolean effective = includeInactive && "ADMIN".equals(role);
        return ResponseEntity.ok(ApiResponse.success("Courses", courseService.getCoursesByDept(deptId, effective)));
    }

    @GetMapping("/api/departments/{deptId}/batches")
    public ResponseEntity<ApiResponse<List<Batch>>> getBatchesByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        boolean effective = includeInactive && "ADMIN".equals(role);
        return ResponseEntity.ok(ApiResponse.success("Batches", courseService.getBatchesByDept(deptId, effective)));
    }

    // ── Courses ───────────────────────────────────────────────────────────

    @PostMapping("/api/courses")
    public ResponseEntity<ApiResponse<Course>> createCourse(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created", courseService.createCourse(req)));
    }

    @GetMapping("/api/courses/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourse(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Course", courseService.getCourseById(id)));
    }

    /** ADMIN only — activate/deactivate a course */
    @PatchMapping("/api/courses/{id}/status")
    public ResponseEntity<ApiResponse<Course>> setCourseActive(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam boolean active) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(
                active ? "Course activated" : "Course deactivated",
                courseService.setCourseActive(id, active)));
    }

    /** ADMIN only — PERMANENTLY delete a course and everything under it */
    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        courseService.deleteCourseCascade(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Course and all its subjects, batches and students permanently deleted", null));
    }

    @GetMapping("/api/courses/{courseId}/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjects(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        boolean effective = includeInactive && "ADMIN".equals(role);
        return ResponseEntity.ok(ApiResponse.success("Subjects",
                courseService.getSubjectsByCourse(courseId, semester, effective)));
    }

    @GetMapping("/api/courses/{courseId}/batches")
    public ResponseEntity<ApiResponse<List<Batch>>> getBatchesByCourse(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        boolean effective = includeInactive && "ADMIN".equals(role);
        return ResponseEntity.ok(ApiResponse.success("Batches", courseService.getBatchesByCourse(courseId, effective)));
    }

    // ── Subjects ──────────────────────────────────────────────────────────

    @PostMapping("/api/subjects")
    public ResponseEntity<ApiResponse<Subject>> createSubject(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created", courseService.createSubject(req)));
    }

    @GetMapping("/api/subjects/{id}")
    public ResponseEntity<ApiResponse<Subject>> getSubject(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        return ResponseEntity.ok(ApiResponse.success("Subject", courseService.getSubjectById(id)));
    }

    /** ADMIN only — activate/deactivate a subject */
    @PatchMapping("/api/subjects/{id}/status")
    public ResponseEntity<ApiResponse<Subject>> setSubjectActive(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam boolean active) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(
                active ? "Subject activated" : "Subject deactivated",
                courseService.setSubjectActive(id, active)));
    }

    /** ADMIN only — PERMANENTLY delete a subject and its attendance/marks history */
    @DeleteMapping("/api/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        courseService.deleteSubjectCascade(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Subject and its attendance/marks history permanently deleted", null));
    }

    // ── Batches ───────────────────────────────────────────────────────────

    @PostMapping("/api/courses/batches")
    public ResponseEntity<ApiResponse<Batch>> createBatch(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch created", courseService.createBatch(req)));
    }

    @GetMapping("/api/courses/batches/{id}")
    public ResponseEntity<ApiResponse<Batch>> getBatch(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Batch", courseService.getBatchById(id)));
    }

    @PatchMapping("/api/courses/batches/{batchId}/semester")
    public ResponseEntity<ApiResponse<Batch>> updateBatchSemester(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long batchId,
            @RequestParam Integer semester) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success("Batch semester updated",
                courseService.updateBatchSemester(batchId, semester)));
    }

    /** ADMIN only — activate/deactivate a batch */
    @PatchMapping("/api/courses/batches/{id}/status")
    public ResponseEntity<ApiResponse<Batch>> setBatchActive(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam boolean active) {
        RoleGuard.requireAdmin(role);
        return ResponseEntity.ok(ApiResponse.success(
                active ? "Batch activated" : "Batch deactivated",
                courseService.setBatchActive(id, active)));
    }

    /** ADMIN only — PERMANENTLY delete a batch and its students */
    @DeleteMapping("/api/courses/batches/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        RoleGuard.requireAdmin(role);
        courseService.deleteBatchCascade(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Batch and all its students permanently deleted", null));
    }
}
