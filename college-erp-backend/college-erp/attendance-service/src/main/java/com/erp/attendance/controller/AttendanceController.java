package com.erp.attendance.controller;

import com.erp.attendance.dto.AttendanceDto;
import com.erp.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management and face recognition endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/bulk")
    @Operation(summary = "Mark attendance for multiple students (manual)")
    public ResponseEntity<AttendanceDto.ApiResponse<List<AttendanceDto.Response>>> markBulk(
            @RequestBody AttendanceDto.BulkMarkRequest request) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Attendance marked", attendanceService.markAttendanceBulk(request)));
    }

    @PostMapping("/face-recognition")
    @Operation(summary = "Mark attendance via face recognition")
    public ResponseEntity<AttendanceDto.ApiResponse<AttendanceDto.Response>> markByFace(
            @RequestBody AttendanceDto.FaceRecognitionMarkRequest request) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Attendance marked via face recognition",
                attendanceService.markByFaceRecognition(request)));
    }

    @PostMapping("/session/start")
    @Operation(summary = "Start a face recognition attendance session")
    public ResponseEntity<AttendanceDto.ApiResponse<AttendanceDto.SessionResponse>> startSession(
            @RequestBody AttendanceDto.StartSessionRequest request) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Session started", attendanceService.startSession(request)));
    }

    @PatchMapping("/session/{sessionId}/end")
    @Operation(summary = "End an attendance session")
    public ResponseEntity<AttendanceDto.ApiResponse<AttendanceDto.SessionResponse>> endSession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Session ended", attendanceService.endSession(sessionId)));
    }

    @GetMapping("/student/{studentId}/subject/{subjectId}/percentage")
    @Operation(summary = "Get attendance percentage for a student in a subject")
    public ResponseEntity<AttendanceDto.ApiResponse<AttendanceDto.AttendancePercentage>> getPercentage(
            @PathVariable Long studentId, @PathVariable Long subjectId) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Attendance percentage fetched",
                attendanceService.getStudentAttendance(studentId, subjectId)));
    }

    @GetMapping("/subject/{subjectId}/date/{date}")
    @Operation(summary = "Get attendance records for a subject on a given date")
    public ResponseEntity<AttendanceDto.ApiResponse<List<AttendanceDto.Response>>> getByDate(
            @PathVariable Long subjectId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Attendance fetched", attendanceService.getAttendanceByDate(subjectId, date)));
    }

    @GetMapping("/subject/{subjectId}/defaulters")
    @Operation(summary = "Get student IDs with attendance below threshold (default 75%)")
    public ResponseEntity<AttendanceDto.ApiResponse<List<Long>>> getDefaulters(
            @PathVariable Long subjectId,
            @RequestParam(required = false) Double threshold) {
        return ResponseEntity.ok(AttendanceDto.ApiResponse.success(
                "Defaulters list fetched",
                attendanceService.getDefaulters(subjectId, threshold)));
    }
}
