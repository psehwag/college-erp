package com.erp.faculty.controller;

import com.erp.faculty.dto.FacultyDto;
import com.erp.faculty.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty", description = "Faculty management and subject assignment endpoints")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    public ResponseEntity<FacultyDto.ApiResponse<FacultyDto.Response>> create(
            @Valid @RequestBody FacultyDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FacultyDto.ApiResponse.success("Faculty created", facultyService.createFaculty(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyDto.ApiResponse<FacultyDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty fetched", facultyService.getById(id)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<FacultyDto.ApiResponse<FacultyDto.Response>> getByEmployeeId(
            @PathVariable String employeeId) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty fetched",
                facultyService.getByEmployeeId(employeeId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyDto.ApiResponse<FacultyDto.Response>> update(
            @PathVariable Long id, @Valid @RequestBody FacultyDto.UpdateRequest req) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty updated", facultyService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FacultyDto.ApiResponse<Void>> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty deactivated", null));
    }

    @GetMapping
    public ResponseEntity<FacultyDto.ApiResponse<Page<FacultyDto.Response>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty list", facultyService.getAll(page, size, sort)));
    }

    @GetMapping("/search")
    public ResponseEntity<FacultyDto.ApiResponse<Page<FacultyDto.Response>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Search results", facultyService.search(q, page, size)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<FacultyDto.ApiResponse<List<FacultyDto.Response>>> getByDepartment(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Faculty by department",
                facultyService.getByDepartment(deptId)));
    }

    // ── Subject Assignments ────────────────────────────────────────────────

    @PostMapping("/assignments")
    @Operation(summary = "Assign a faculty member to a subject and batch")
    public ResponseEntity<FacultyDto.ApiResponse<FacultyDto.AssignmentResponse>> assignSubject(
            @Valid @RequestBody FacultyDto.AssignSubjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FacultyDto.ApiResponse.success("Subject assigned",
                        facultyService.assignSubject(req)));
    }

    @GetMapping("/{facultyId}/assignments")
    @Operation(summary = "Get all subject assignments for a faculty member")
    public ResponseEntity<FacultyDto.ApiResponse<List<FacultyDto.AssignmentResponse>>> getAssignments(
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Assignments fetched",
                facultyService.getAssignmentsByFaculty(facultyId)));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @Operation(summary = "Remove a subject assignment")
    public ResponseEntity<FacultyDto.ApiResponse<Void>> removeAssignment(@PathVariable Long assignmentId) {
        facultyService.removeAssignment(assignmentId);
        return ResponseEntity.ok(FacultyDto.ApiResponse.success("Assignment removed", null));
    }
}
