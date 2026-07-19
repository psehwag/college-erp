package com.erp.face.controller;

import com.erp.face.dto.FaceDto;
import com.erp.face.service.FaceRecognitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
@Tag(name = "Face Recognition", description = "AI-powered face recognition for attendance")
public class FaceRecognitionController {

    private final FaceRecognitionService faceRecognitionService;

    @PostMapping(value = "/enroll/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enroll student face - upload 5-10 clear photos")
    public ResponseEntity<FaceDto.ApiResponse<FaceDto.EnrollmentResponse>> enrollFace(
            @PathVariable Long studentId,
            @RequestParam("photos") List<MultipartFile> photos) {
        return ResponseEntity.ok(FaceDto.ApiResponse.success(
                "Face enrolled", faceRecognitionService.enrollStudentFace(studentId, photos)));
    }

    @PostMapping("/train")
    @Operation(summary = "Train LBPH model for a batch (admin/faculty only)")
    public ResponseEntity<FaceDto.ApiResponse<FaceDto.TrainingResponse>> trainModel(
            @RequestBody FaceDto.TrainingRequest request) {
        return ResponseEntity.ok(FaceDto.ApiResponse.success(
                "Model trained", faceRecognitionService.trainModel(request.getBatchId(), request.getStudentIds())));
    }

    @PostMapping("/recognize")
    @Operation(summary = "Recognize a face from a Base64-encoded webcam frame")
    public ResponseEntity<FaceDto.ApiResponse<FaceDto.RecognitionResult>> recognizeFace(
            @RequestBody FaceDto.RecognitionRequest request) {
        byte[] imageBytes = Base64.getDecoder().decode(request.getFrameBase64());
        FaceDto.RecognitionResult result = faceRecognitionService.recognizeFace(request.getBatchId(), imageBytes);
        return ResponseEntity.ok(FaceDto.ApiResponse.success("Recognition complete", result));
    }

    @GetMapping("/enrolled/{studentId}")
    @Operation(summary = "Check if student face is enrolled")
    public ResponseEntity<FaceDto.ApiResponse<Boolean>> checkEnrolled(@PathVariable Long studentId) {
        return ResponseEntity.ok(FaceDto.ApiResponse.success(
                "Enrollment status", faceRecognitionService.isStudentEnrolled(studentId)));
    }

    @DeleteMapping("/enroll/{studentId}")
    @Operation(summary = "Delete student face enrollment")
    public ResponseEntity<FaceDto.ApiResponse<Void>> deleteEnrollment(@PathVariable Long studentId) {
        faceRecognitionService.deleteEnrollment(studentId);
        return ResponseEntity.ok(FaceDto.ApiResponse.success("Enrollment deleted", null));
    }
}
