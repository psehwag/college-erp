package com.erp.face.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.util.RoleGuard;
import com.erp.face.dto.FaceDto;
import com.erp.face.service.FaceRecognitionService;
import com.erp.student.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {

    private final FaceRecognitionService faceService;
    private final StudentService studentService;

    public FaceRecognitionController(FaceRecognitionService faceService, StudentService studentService) {
        this.faceService = faceService;
        this.studentService = studentService;
    }

    /** ADMIN only — enroll a student's face photos */
    @PostMapping(value = "/enroll/{studentId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<FaceDto.EnrollmentResponse>> enroll(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long studentId,
            @RequestParam("photos") List<MultipartFile> photos) {
        RoleGuard.requireAdmin(role);
        FaceDto.EnrollmentResponse res = faceService.enrollStudentFace(studentId, photos);
        studentService.markFaceEnrolled(studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res.getMessage(), res));
    }

    /** ADMIN only — train the LBPH model for a batch */
    @PostMapping("/train")
    public ResponseEntity<ApiResponse<FaceDto.TrainingResponse>> train(
            @RequestHeader("X-User-Role") String role,
            @RequestBody FaceDto.TrainingRequest req) {
        RoleGuard.requireAdmin(role);
        FaceDto.TrainingResponse res = faceService.trainModel(req.getBatchId(), req.getStudentIds());
        return ResponseEntity.ok(ApiResponse.success(res.getMessage(), res));
    }

    /** ADMIN, FACULTY — recognize a face frame during a live attendance session */
    @PostMapping("/recognize")
    public ResponseEntity<ApiResponse<FaceDto.RecognitionResult>> recognize(
            @RequestHeader("X-User-Role") String role,
            @RequestBody FaceDto.RecognitionRequest req) {
        RoleGuard.requireAdminOrFaculty(role);
        byte[] bytes = Base64.getDecoder().decode(req.getFrameBase64());
        FaceDto.RecognitionResult res = faceService.recognizeFace(req.getBatchId(), bytes);
        return ResponseEntity.ok(ApiResponse.success(res.getMessage(), res));
    }

    /** ADMIN, FACULTY — check if a student already has enrolled face data */
    @GetMapping("/enrolled/{studentId}")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrolled(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long studentId) {
        RoleGuard.requireAdminOrFaculty(role);
        return ResponseEntity.ok(ApiResponse.success("Enrollment status",
                faceService.isStudentEnrolled(studentId)));
    }

    /** ADMIN only — remove a student's enrolled face data */
    @DeleteMapping("/enroll/{studentId}")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long studentId) {
        RoleGuard.requireAdmin(role);
        faceService.deleteEnrollment(studentId);
        return ResponseEntity.ok(ApiResponse.success("Face enrollment removed", null));
    }
}
