package com.erp.face.service;

import com.erp.face.dto.FaceDto;
import com.erp.face.entity.FaceEnrollment;
import com.erp.face.exception.FaceRecognitionException;
import com.erp.face.repository.FaceEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.LBPHFaceRecognizer;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FaceRecognitionService {

    private final FaceEnrollmentRepository enrollmentRepository;

    @Value("${face.model.path:./face-models}")
    private String modelBasePath;

    @Value("${face.confidence.threshold:80.0}")
    private double confidenceThreshold;

    @Value("${face.haar.cascade.path:classpath:haarcascade_frontalface_default.xml}")
    private String haarCascadePath;

    // Cache trained models per batch/subject to avoid reloading
    private final ConcurrentHashMap<String, LBPHFaceRecognizer> modelCache = new ConcurrentHashMap<>();

    private CascadeClassifier faceDetector;

    @PostConstruct
    public void init() {
        try {
            // Load Haar cascade for face detection
            String cascadePath = extractHaarCascade();
            faceDetector = new CascadeClassifier(cascadePath);
            if (faceDetector.empty()) {
                log.error("Failed to load Haar cascade classifier. Ensure haarcascade_frontalface_default.xml is in resources.");
            } else {
                log.info("Haar cascade face detector loaded successfully");
            }

            // Ensure model directory exists
            Files.createDirectories(Paths.get(modelBasePath));
            log.info("Face Recognition Service initialized. Model path: {}", modelBasePath);
        } catch (Exception e) {
            log.error("Failed to initialize Face Recognition Service: {}", e.getMessage());
        }
    }

    /**
     * Enroll student face: accept multiple photos, detect faces, save to DB.
     */
    public FaceDto.EnrollmentResponse enrollStudentFace(Long studentId, List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) {
            throw new FaceRecognitionException("At least one photo is required for enrollment");
        }

        List<String> savedPaths = new ArrayList<>();
        int successCount = 0;

        for (MultipartFile photo : photos) {
            try {
                byte[] imageBytes = photo.getBytes();
                Mat image = imdecode(new Mat(imageBytes), IMREAD_GRAYSCALE);

                if (image.empty()) {
                    log.warn("Could not decode image for student {}", studentId);
                    continue;
                }

                // Detect face in the image
                MatOfRect faces = detectFaces(image);
                if (faces.empty()) {
                    log.warn("No face detected in uploaded photo for student {}", studentId);
                    continue;
                }

                // Save the face region
                Rect faceRect = faces.toArray()[0];
                Mat faceROI = new Mat(image, faceRect);
                Mat resizedFace = resizeFace(faceROI);

                // Save raw bytes to DB
                byte[] faceBytes = matToBytes(resizedFace);
                FaceEnrollment enrollment = FaceEnrollment.builder()
                        .studentId(studentId)
                        .faceImageData(faceBytes)
                        .imageIndex(successCount)
                        .isActive(true)
                        .build();
                enrollmentRepository.save(enrollment);
                successCount++;

                image.release();
                faceROI.release();
                resizedFace.release();

            } catch (IOException e) {
                log.error("Error processing photo for student {}: {}", studentId, e.getMessage());
            }
        }

        if (successCount == 0) {
            throw new FaceRecognitionException("No valid face detected in any of the uploaded photos");
        }

        // Invalidate cached models so they'll be retrained
        modelCache.clear();

        log.info("Enrolled {} face images for student {}", successCount, studentId);
        return FaceDto.EnrollmentResponse.builder()
                .studentId(studentId)
                .enrolledImages(successCount)
                .message("Face enrollment successful")
                .build();
    }

    /**
     * Train LBPH model for a given batch using enrolled faces.
     * Called after enrollment or when model needs refresh.
     */
    public FaceDto.TrainingResponse trainModel(Long batchId, List<Long> studentIds) {
        log.info("Training LBPH model for batch: {}", batchId);

        List<Mat> faceImages = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        for (Long studentId : studentIds) {
            List<FaceEnrollment> enrollments = enrollmentRepository.findByStudentIdAndIsActiveTrue(studentId);
            if (enrollments.isEmpty()) {
                log.warn("No enrolled faces for student: {}", studentId);
                continue;
            }
            for (FaceEnrollment enrollment : enrollments) {
                Mat faceImg = bytesToMat(enrollment.getFaceImageData());
                if (!faceImg.empty()) {
                    faceImages.add(faceImg);
                    labels.add(studentId.intValue());
                }
            }
        }

        if (faceImages.isEmpty()) {
            throw new FaceRecognitionException("No enrolled face data found for training");
        }

        // Train LBPH recognizer
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        MatVector imageVector = new MatVector(faceImages.toArray(new Mat[0]));
        Mat labelMat = new Mat(labels.size(), 1, CV_32SC1);

        for (int i = 0; i < labels.size(); i++) {
            labelMat.ptr(i).putInt(labels.get(i));
        }

        recognizer.train(imageVector, labelMat);

        // Save model to disk
        String modelPath = getModelPath(batchId);
        recognizer.save(modelPath);

        // Cache in memory
        String cacheKey = "batch_" + batchId;
        modelCache.put(cacheKey, recognizer);

        // Cleanup
        faceImages.forEach(Mat::release);
        labelMat.release();

        log.info("LBPH model trained with {} samples for batch {}", faceImages.size(), batchId);
        return FaceDto.TrainingResponse.builder()
                .batchId(batchId)
                .trainedSamples(faceImages.size())
                .uniqueStudents(studentIds.size())
                .modelPath(modelPath)
                .message("Model trained successfully")
                .build();
    }

    /**
     * Recognize a face in an uploaded image frame against a batch model.
     */
    public FaceDto.RecognitionResult recognizeFace(Long batchId, byte[] imageBytes) {
        // Get or load model
        LBPHFaceRecognizer recognizer = getOrLoadModel(batchId);
        if (recognizer == null) {
            throw new FaceRecognitionException("No trained model found for batch: " + batchId +
                    ". Please train the model first.");
        }

        Mat image = imdecode(new Mat(imageBytes), IMREAD_GRAYSCALE);
        if (image.empty()) {
            throw new FaceRecognitionException("Cannot decode uploaded image frame");
        }

        MatOfRect faces = detectFaces(image);
        if (faces.empty()) {
            image.release();
            return FaceDto.RecognitionResult.builder()
                    .recognized(false)
                    .message("No face detected in the frame")
                    .build();
        }

        Rect faceRect = faces.toArray()[0];
        Mat faceROI = new Mat(image, faceRect);
        Mat resizedFace = resizeFace(faceROI);

        int[] label = new int[1];
        double[] confidence = new double[1];
        recognizer.predict(resizedFace, label, confidence);

        image.release();
        faceROI.release();
        resizedFace.release();

        // Lower confidence value = better match in LBPH
        boolean recognized = confidence[0] <= confidenceThreshold;
        double normalizedScore = Math.max(0, 100 - confidence[0]);

        log.info("Face recognition result: studentId={}, confidence={}, recognized={}",
                label[0], confidence[0], recognized);

        return FaceDto.RecognitionResult.builder()
                .recognized(recognized)
                .studentId(recognized ? (long) label[0] : null)
                .confidenceScore(normalizedScore)
                .rawConfidence(confidence[0])
                .message(recognized ? "Face recognized" : "Face not recognized (confidence too low)")
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private MatOfRect detectFaces(Mat grayImage) {
        MatOfRect faces = new MatOfRect();
        if (faceDetector != null && !faceDetector.empty()) {
            faceDetector.detectMultiScale(grayImage, faces, 1.1, 3, 0,
                    new Size(30, 30), new Size());
        }
        return faces;
    }

    private Mat resizeFace(Mat face) {
        Mat resized = new Mat();
        opencv_imgproc.resize(face, resized, new Size(200, 200));
        return resized;
    }

    private LBPHFaceRecognizer getOrLoadModel(Long batchId) {
        String cacheKey = "batch_" + batchId;
        if (modelCache.containsKey(cacheKey)) {
            return modelCache.get(cacheKey);
        }

        String modelPath = getModelPath(batchId);
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            return null;
        }

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(modelPath);
        modelCache.put(cacheKey, recognizer);
        return recognizer;
    }

    private String getModelPath(Long batchId) {
        return modelBasePath + "/lbph_batch_" + batchId + ".xml";
    }

    private byte[] matToBytes(Mat mat) {
        MatOfByte mob = new MatOfByte();
        opencv_imgcodecs.imencode(".jpg", mat, mob);
        return mob.toArray();
    }

    private Mat bytesToMat(byte[] bytes) {
        Mat compressed = new Mat(bytes);
        return imdecode(compressed, IMREAD_GRAYSCALE);
    }

    private String extractHaarCascade() throws IOException {
        // Extract from classpath to temp file
        var is = getClass().getClassLoader().getResourceAsStream("haarcascade_frontalface_default.xml");
        if (is == null) {
            // Fallback to default OpenCV location
            return "haarcascade_frontalface_default.xml";
        }
        Path tempPath = Files.createTempFile("haar_cascade", ".xml");
        Files.write(tempPath, is.readAllBytes());
        return tempPath.toString();
    }

    private Mat imdecode(Mat buffer, int flags) {
        return opencv_imgcodecs.imdecode(buffer, flags);
    }

    public boolean isStudentEnrolled(Long studentId) {
        return enrollmentRepository.existsByStudentIdAndIsActiveTrue(studentId);
    }

    public void deleteEnrollment(Long studentId) {
        enrollmentRepository.deactivateByStudentId(studentId);
        modelCache.clear();
        log.info("Face enrollment deleted for student: {}", studentId);
    }
}
