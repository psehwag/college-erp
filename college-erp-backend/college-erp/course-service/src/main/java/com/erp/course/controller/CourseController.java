package com.erp.course.controller;

import com.erp.course.dto.CourseDto;
import com.erp.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ── Department Endpoints ──────────────────────────────────────────────

    @PostMapping("/api/departments")
    @Tag(name = "Departments")
    @Operation(summary = "Create department")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.DeptResponse>> createDept(
            @Valid @RequestBody CourseDto.DeptCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseDto.ApiResponse.success("Department created", courseService.createDepartment(req)));
    }

    @GetMapping("/api/departments")
    @Tag(name = "Departments")
    @Operation(summary = "Get all active departments")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.DeptResponse>>> getAllDepts() {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Departments fetched",
                courseService.getAllDepartments()));
    }

    @GetMapping("/api/departments/{id}")
    @Tag(name = "Departments")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.DeptResponse>> getDeptById(@PathVariable Long id) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Department fetched",
                courseService.getDepartmentById(id)));
    }

    @PutMapping("/api/departments/{id}")
    @Tag(name = "Departments")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.DeptResponse>> updateDept(
            @PathVariable Long id, @RequestBody CourseDto.DeptCreateRequest req) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Department updated",
                courseService.updateDepartment(id, req)));
    }

    // ── Course Endpoints ──────────────────────────────────────────────────

    @PostMapping("/api/courses")
    @Tag(name = "Courses")
    @Operation(summary = "Create course")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.CourseResponse>> createCourse(
            @Valid @RequestBody CourseDto.CourseCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseDto.ApiResponse.success("Course created", courseService.createCourse(req)));
    }

    @GetMapping("/api/courses/{id}")
    @Tag(name = "Courses")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Course fetched", courseService.getCourseById(id)));
    }

    @GetMapping("/api/departments/{deptId}/courses")
    @Tag(name = "Courses")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.CourseResponse>>> getCoursesByDept(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Courses fetched",
                courseService.getCoursesByDepartment(deptId)));
    }

    // ── Subject Endpoints ─────────────────────────────────────────────────

    @PostMapping("/api/subjects")
    @Tag(name = "Subjects")
    @Operation(summary = "Create subject and assign to course + semester")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.SubjectResponse>> createSubject(
            @Valid @RequestBody CourseDto.SubjectCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseDto.ApiResponse.success("Subject created", courseService.createSubject(req)));
    }

    @GetMapping("/api/subjects/{id}")
    @Tag(name = "Subjects")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.SubjectResponse>> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Subject fetched", courseService.getSubjectById(id)));
    }

    @GetMapping("/api/courses/{courseId}/subjects")
    @Tag(name = "Subjects")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.SubjectResponse>>> getSubjectsByCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer semester) {
        List<CourseDto.SubjectResponse> subjects = semester != null
                ? courseService.getSubjectsByCourseAndSemester(courseId, semester)
                : courseService.getSubjectsByCourse(courseId);
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Subjects fetched", subjects));
    }

    // ── Batch Endpoints ───────────────────────────────────────────────────

    @PostMapping("/api/courses/batches")
    @Tag(name = "Batches")
    @Operation(summary = "Create a student batch")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.BatchResponse>> createBatch(
            @Valid @RequestBody CourseDto.BatchCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CourseDto.ApiResponse.success("Batch created", courseService.createBatch(req)));
    }

    @GetMapping("/api/courses/batches/{id}")
    @Tag(name = "Batches")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.BatchResponse>> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Batch fetched", courseService.getBatchById(id)));
    }

    @GetMapping("/api/courses/{courseId}/batches")
    @Tag(name = "Batches")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.BatchResponse>>> getBatchesByCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Batches fetched",
                courseService.getBatchesByCourse(courseId)));
    }

    @GetMapping("/api/departments/{deptId}/batches")
    @Tag(name = "Batches")
    public ResponseEntity<CourseDto.ApiResponse<List<CourseDto.BatchResponse>>> getBatchesByDept(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Batches fetched",
                courseService.getBatchesByDepartment(deptId)));
    }

    @PatchMapping("/api/courses/batches/{batchId}/semester")
    @Tag(name = "Batches")
    @Operation(summary = "Update batch current semester (semester promotion)")
    public ResponseEntity<CourseDto.ApiResponse<CourseDto.BatchResponse>> updateSemester(
            @PathVariable Long batchId, @RequestParam Integer semester) {
        return ResponseEntity.ok(CourseDto.ApiResponse.success("Batch semester updated",
                courseService.updateBatchSemester(batchId, semester)));
    }
}
