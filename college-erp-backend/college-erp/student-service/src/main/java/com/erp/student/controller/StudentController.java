package com.erp.student.controller;

import com.erp.student.dto.StudentDto;
import com.erp.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @Operation(summary = "Create a new student")
    public ResponseEntity<StudentDto.ApiResponse<StudentDto.Response>> createStudent(
            @Valid @RequestBody StudentDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StudentDto.ApiResponse.success("Student created", studentService.createStudent(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<StudentDto.ApiResponse<StudentDto.Response>> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Student fetched", studentService.getStudentById(id)));
    }

    @GetMapping("/enrollment/{enrollmentNumber}")
    @Operation(summary = "Get student by enrollment number")
    public ResponseEntity<StudentDto.ApiResponse<StudentDto.Response>> getByEnrollment(
            @PathVariable String enrollmentNumber) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Student fetched",
                studentService.getStudentByEnrollment(enrollmentNumber)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update student details")
    public ResponseEntity<StudentDto.ApiResponse<StudentDto.Response>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDto.UpdateRequest request) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Student updated", studentService.updateStudent(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a student")
    public ResponseEntity<StudentDto.ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Student deactivated", null));
    }

    @GetMapping
    @Operation(summary = "Get all students with pagination")
    public ResponseEntity<StudentDto.ApiResponse<Page<StudentDto.Response>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort) {
        Page<StudentDto.Response> students = studentService.getAllStudents(page, size, sort);
        return ResponseEntity.ok(StudentDto.ApiResponse.<Page<StudentDto.Response>>builder()
                .success(true).message("Students fetched").data(students)
                .total(students.getTotalElements()).build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search students")
    public ResponseEntity<StudentDto.ApiResponse<Page<StudentDto.Response>>> searchStudents(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Search results",
                studentService.searchStudents(q, page, size)));
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get students by batch")
    public ResponseEntity<StudentDto.ApiResponse<List<StudentDto.Summary>>> getByBatch(
            @PathVariable Long batchId) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Students fetched",
                studentService.getStudentsByBatch(batchId)));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get students by department")
    public ResponseEntity<StudentDto.ApiResponse<List<StudentDto.Summary>>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Students fetched",
                studentService.getStudentsByDepartment(departmentId)));
    }

    @PatchMapping("/{id}/face-enrolled")
    @Operation(summary = "Mark student face as enrolled")
    public ResponseEntity<StudentDto.ApiResponse<Void>> markFaceEnrolled(@PathVariable Long id) {
        studentService.markFaceEnrolled(id);
        return ResponseEntity.ok(StudentDto.ApiResponse.success("Face enrollment updated", null));
    }
}
