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
            @RequestHeader("X-User-Role") String role) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        return ResponseEntity.ok(ApiResponse.success("Departments", courseService.getAllDepartments()));
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

    @GetMapping("/api/departments/{deptId}/courses")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Courses", courseService.getCoursesByDept(deptId)));
    }

    @GetMapping("/api/departments/{deptId}/batches")
    public ResponseEntity<ApiResponse<List<Batch>>> getBatchesByDept(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long deptId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Batches", courseService.getBatchesByDept(deptId)));
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

    @GetMapping("/api/courses/{courseId}/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjects(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer semester) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Subjects",
                courseService.getSubjectsByCourse(courseId, semester)));
    }

    @GetMapping("/api/courses/{courseId}/batches")
    public ResponseEntity<ApiResponse<List<Batch>>> getBatchesByCourse(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long courseId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Batches", courseService.getBatchesByCourse(courseId)));
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
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT");
        return ResponseEntity.ok(ApiResponse.success("Subject", courseService.getSubjectById(id)));
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
}
