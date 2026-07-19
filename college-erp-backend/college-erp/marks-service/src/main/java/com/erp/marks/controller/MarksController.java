package com.erp.marks.controller;

import com.erp.marks.dto.MarksDto;
import com.erp.marks.entity.Marks;
import com.erp.marks.service.MarksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
@Tag(name = "Marks", description = "Academic marks management endpoints")
public class MarksController {

    private final MarksService marksService;

    @PostMapping
    @Operation(summary = "Upload or update marks for a single student-subject-exam")
    public ResponseEntity<MarksDto.ApiResponse<MarksDto.Response>> upsert(
            @Valid @RequestBody MarksDto.UpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MarksDto.ApiResponse.success("Marks saved", marksService.upsertMarks(req)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Upload marks for entire batch in one shot")
    public ResponseEntity<MarksDto.ApiResponse<List<MarksDto.Response>>> bulkUpsert(
            @Valid @RequestBody MarksDto.BulkUpsertRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MarksDto.ApiResponse.success("Bulk marks saved", marksService.bulkUpsert(req)));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all marks for a student")
    public ResponseEntity<MarksDto.ApiResponse<List<MarksDto.Response>>> getByStudent(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(MarksDto.ApiResponse.success("Marks fetched",
                marksService.getMarksByStudent(studentId)));
    }

    @GetMapping("/student/{studentId}/semester/{semester}/summary")
    @Operation(summary = "Get semester result summary for a student")
    public ResponseEntity<MarksDto.ApiResponse<MarksDto.StudentSummary>> getSemesterSummary(
            @PathVariable Long studentId, @PathVariable Integer semester) {
        return ResponseEntity.ok(MarksDto.ApiResponse.success("Semester summary fetched",
                marksService.getStudentSemesterSummary(studentId, semester)));
    }

    @GetMapping("/batch/{batchId}/subject/{subjectId}")
    @Operation(summary = "Get marks for a whole batch in a subject and exam type")
    public ResponseEntity<MarksDto.ApiResponse<List<MarksDto.Response>>> getBatchMarks(
            @PathVariable Long batchId,
            @PathVariable Long subjectId,
            @RequestParam Marks.ExamType examType) {
        return ResponseEntity.ok(MarksDto.ApiResponse.success("Batch marks fetched",
                marksService.getMarksByBatchSubjectExam(batchId, subjectId, examType)));
    }
}
