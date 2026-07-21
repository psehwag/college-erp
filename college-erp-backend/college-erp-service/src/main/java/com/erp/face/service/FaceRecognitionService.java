package com.erp.face.service;

import com.erp.common.exception.AppException;
import com.erp.face.dto.FaceDto;
import com.erp.face.entity.FaceEnrollment;
import com.erp.face.repository.FaceEnrollmentRepository;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;

@Service
@Transactional
public class FaceRecognitionService {

    private static final Logger log = Logger.getLogger(FaceRecognitionService.class.getName());

    private final FaceEnrollmentRepository enrollmentRepository;

    @Value("${face.model.path:./face-models}")
    private String modelBasePath;

    @Value("${face.confidence.threshold:80.0}")
    private double confidenceThreshold;

    private final Map<String, LBPHFaceRecognizer> modelCache = new ConcurrentHashMap<>();
    private CascadeClassifier faceDetector;
    private boolean cascadeLoaded = false;

    public FaceRecognitionService(FaceEnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(modelBasePath));

            try (InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("haarcascade_frontalface_default.xml")) {

                if (is == null) {
                    log.severe("haarcascade_frontalface_default.xml not found on classpath. " +
                            "Face detection will not work until this file is placed in " +
                            "college-erp-service/src/main/resources/ and the service is rebuilt.");
                    return;
                }

                Path tempPath = Files.createTempFile("haarcascade", ".xml");
                Files.write(tempPath, is.readAllBytes());
                faceDetector = new CascadeClassifier(tempPath.toString());

                if (faceDetector.empty()) {
                    log.severe("Haar cascade file loaded but classifier is empty (corrupt file?)");
                } else {
                    cascadeLoaded = true;
                    log.info("Haar cascade loaded successfully. Face Recognition Service ready.");
                }
            }
        } catch (Exception e) {
            log.severe("Failed to initialize Face Recognition Service: " + e.getMessage());
        }
    }

    private void requireCascade() {
        if (!cascadeLoaded) {
            throw new AppException(
                    "Face detector is not initialized on the server (missing haarcascade_frontalface_default.xml " +
                    "in college-erp-service resources). Ask the administrator to add this file and rebuild.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // ── Enroll ────────────────────────────────────────────────────────────

    public FaceDto.EnrollmentResponse enrollStudentFace(Long studentId, List<MultipartFile> photos) {
        requireCascade();
        if (photos == null || photos.isEmpty()) {
            throw new AppException("At least one photo is required", HttpStatus.BAD_REQUEST);
        }

        int successCount = 0;
        int existingCount = (int) enrollmentRepository.findByStudentIdAndIsActiveTrue(studentId).size();

        for (MultipartFile photo : photos) {
            Mat image = null, faceROI = null, resized = null;
            try {
                byte[] bytes = photo.getBytes();
                image = decodeGrayscale(bytes);
                if (image == null || image.empty()) {
                    log.warning("Could not decode image for student " + studentId);
                    continue;
                }

                RectVector faces = detectFaces(image);
                if (faces.size() == 0) {
                    log.warning("No face detected in one of the uploaded photos for student " + studentId);
                    continue;
                }

                Rect faceRect = faces.get(0);
                faceROI = new Mat(image, faceRect);
                resized = resizeFace(faceROI);
                byte[] faceBytes = matToBytes(resized);

                FaceEnrollment fe = new FaceEnrollment();
                fe.setStudentId(studentId);
                fe.setFaceImageData(faceBytes);
                fe.setImageIndex(existingCount + successCount);
                fe.setIsActive(true);
                enrollmentRepository.save(fe);

                successCount++;
            } catch (IOException e) {
                log.warning("Error reading uploaded photo for student " + studentId + ": " + e.getMessage());
            } finally {
                if (image != null) image.release();
                if (faceROI != null) faceROI.release();
                if (resized != null) resized.release();
            }
        }

        if (successCount == 0) {
            throw new AppException(
                    "No valid face could be detected in any of the uploaded photos. " +
                    "Please use clear, front-facing, well-lit photos.",
                    HttpStatus.BAD_REQUEST);
        }

        modelCache.clear();
        log.info("Enrolled " + successCount + " face image(s) for student " + studentId);

        return new FaceDto.EnrollmentResponse(studentId, successCount,
                "Enrolled " + successCount + " photo(s) successfully");
    }

    // ── Train ─────────────────────────────────────────────────────────────

    public FaceDto.TrainingResponse trainModel(Long batchId, List<Long> studentIds) {
        requireCascade();
        if (studentIds == null || studentIds.isEmpty()) {
            throw new AppException("No students in this batch to train on", HttpStatus.BAD_REQUEST);
        }

        List<Mat> images = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        Set<Long> studentsWithData = new HashSet<>();

        try {
            for (Long studentId : studentIds) {
                List<FaceEnrollment> enrollments = enrollmentRepository.findByStudentIdAndIsActiveTrue(studentId);
                for (FaceEnrollment enrollment : enrollments) {
                    Mat img = decodeGrayscale(enrollment.getFaceImageData());
                    if (img != null && !img.empty()) {
                        images.add(img);
                        labels.add(studentId.intValue());
                        studentsWithData.add(studentId);
                    }
                }
            }

            if (images.isEmpty()) {
                throw new AppException(
                        "No enrolled face photos found for any student in this batch. " +
                        "Enroll at least one student's face before training.",
                        HttpStatus.BAD_REQUEST);
            }

            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();

            MatVector imageVector = new MatVector(images.size());
            for (int i = 0; i < images.size(); i++) {
                imageVector.put(i, images.get(i));
            }

            Mat labelMat = new Mat(labels.size(), 1, CV_32SC1);
            IntPointer labelPtr = new IntPointer(labelMat.data());
            for (int i = 0; i < labels.size(); i++) {
                labelPtr.put(i, labels.get(i));
            }

            recognizer.train(imageVector, labelMat);

            String modelPath = getModelPath(batchId);
            recognizer.save(modelPath);
            modelCache.put("batch_" + batchId, recognizer);

            labelMat.release();

            log.info("LBPH model trained: batch=" + batchId + " samples=" + images.size() +
                    " students=" + studentsWithData.size());

            return new FaceDto.TrainingResponse(batchId, images.size(), studentsWithData.size(),
                    "Model trained successfully with " + images.size() + " sample(s) from " +
                    studentsWithData.size() + " student(s)");

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.severe("Training failed for batch " + batchId + ": " + e.getMessage());
            throw new AppException("Training failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            images.forEach(Mat::release);
        }
    }

    // ── Recognize ─────────────────────────────────────────────────────────

    public FaceDto.RecognitionResult recognizeFace(Long batchId, byte[] imageBytes) {
        requireCascade();
        LBPHFaceRecognizer recognizer = getOrLoadModel(batchId);
        if (recognizer == null) {
            throw new AppException(
                    "No trained model found for this batch. Train the model on the Face Enrollment page first.",
                    HttpStatus.BAD_REQUEST);
        }

        Mat image = null, faceROI = null, resized = null;
        try {
            image = decodeGrayscale(imageBytes);
            if (image == null || image.empty()) {
                throw new AppException("Cannot decode camera frame", HttpStatus.BAD_REQUEST);
            }

            RectVector faces = detectFaces(image);
            if (faces.size() == 0) {
                return new FaceDto.RecognitionResult(false, null, null, "No face detected in frame");
            }

            Rect faceRect = faces.get(0);
            faceROI = new Mat(image, faceRect);
            resized = resizeFace(faceROI);

            IntPointer labelPtr = new IntPointer(1);
            DoublePointer confPtr = new DoublePointer(1);
            recognizer.predict(resized, labelPtr, confPtr);

            int predictedId = labelPtr.get(0);
            double rawConf = confPtr.get(0);
            boolean recognized = rawConf <= confidenceThreshold;
            double score = Math.max(0, 100 - rawConf);

            return new FaceDto.RecognitionResult(
                    recognized,
                    recognized ? (long) predictedId : null,
                    score,
                    recognized ? "Face recognized" : "Face not recognized (below confidence threshold)");

        } finally {
            if (image != null) image.release();
            if (faceROI != null) faceROI.release();
            if (resized != null) resized.release();
        }
    }

    // ── Status / cleanup ──────────────────────────────────────────────────

    public boolean isStudentEnrolled(Long studentId) {
        return enrollmentRepository.existsByStudentIdAndIsActiveTrue(studentId);
    }

    public void deleteEnrollment(Long studentId) {
        enrollmentRepository.deactivateByStudentId(studentId);
        modelCache.clear();
        log.info("Face enrollment deactivated for student " + studentId);
    }

    /** Hard-remove all enrollment rows for a student (used when the student is permanently deleted). */
    public void hardDeleteByStudent(Long studentId) {
        enrollmentRepository.deleteByStudentId(studentId);
        modelCache.clear();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private RectVector detectFaces(Mat grayImage) {
        RectVector faces = new RectVector();
        if (faceDetector != null && !faceDetector.empty()) {
            faceDetector.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(30, 30), new Size());
        }
        return faces;
    }

    private Mat resizeFace(Mat face) {
        Mat resized = new Mat();
        opencv_imgproc.resize(face, resized, new Size(200, 200));
        return resized;
    }

    private Mat decodeGrayscale(byte[] bytes) {
        Mat buf = new Mat(1, bytes.length, CV_8UC1);
        buf.data().put(bytes, 0, bytes.length);
        Mat result = imdecode(buf, IMREAD_GRAYSCALE);
        buf.release();
        return result;
    }

    private byte[] matToBytes(Mat mat) {
        BytePointer buf = new BytePointer();
        opencv_imgcodecs.imencode(".jpg", mat, buf);
        byte[] result = new byte[(int) buf.limit()];
        buf.get(result);
        buf.deallocate();
        return result;
    }

    private LBPHFaceRecognizer getOrLoadModel(Long batchId) {
        String key = "batch_" + batchId;
        if (modelCache.containsKey(key)) return modelCache.get(key);

        java.io.File modelFile = new java.io.File(getModelPath(batchId));
        if (!modelFile.exists()) return null;

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(getModelPath(batchId));
        modelCache.put(key, recognizer);
        return recognizer;
    }

    private String getModelPath(Long batchId) {
        return modelBasePath + "/lbph_batch_" + batchId + ".xml";
    }
}
