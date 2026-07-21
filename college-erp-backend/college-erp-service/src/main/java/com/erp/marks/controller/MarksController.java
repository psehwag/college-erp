package com.erp.marks.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.marks.entity.Marks;
import com.erp.marks.service.MarksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marks")
public class MarksController {

    private final MarksService marksService;

    public MarksController(MarksService marksService) {
        this.marksService = marksService;
    }

    /** ADMIN, FACULTY — upload a single mark (no exam type/academic year needed — auto-derived from batch) */
    @PostMapping
    public ResponseEntity<ApiResponse<Marks>> upsert(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Marks saved", marksService.upsert(req)));
    }

    /** ADMIN, FACULTY — bulk upload for entire batch */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<Marks>>> bulkUpsert(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Map<String, Object> req) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk marks saved", marksService.bulkUpsert(req)));
    }

    /** STUDENT — own marks. ADMIN, FACULTY, PARENT — any student */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Marks>>> getByStudent(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long studentId) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, studentId, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Marks fetched",
                marksService.getByStudent(studentId)));
    }

    /** All — semester summary */
    @GetMapping("/student/{studentId}/semester/{semester}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSemSummary(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Reference-Id") String referenceId,
            @PathVariable Long studentId,
            @PathVariable Integer semester) {
        RoleGuard.requireAnyRole(role, "ADMIN", "FACULTY", "STUDENT", "PARENT");
        if ("STUDENT".equals(role)) {
            RoleGuard.requireOwnerOrAdmin(role, studentId, parseLong(referenceId));
        }
        return ResponseEntity.ok(ApiResponse.success("Semester summary",
                marksService.getSemesterSummary(studentId, semester)));
    }

    /** ADMIN, FACULTY — batch marks, enriched with student name + enrollment number */
    @GetMapping("/batch/{batchId}/subject/{subjectId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBatchMarks(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long batchId,
            @PathVariable Long subjectId) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Batch marks fetched",
                marksService.getBatchMarksEnriched(batchId, subjectId)));
    }

    private Long parseLong(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return null; }
    }
}
