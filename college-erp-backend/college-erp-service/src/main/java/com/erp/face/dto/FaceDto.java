package com.erp.face.dto;

import java.util.List;

public class FaceDto {

    public static class EnrollmentResponse {
        private Long studentId;
        private int enrolledImages;
        private String message;

        public EnrollmentResponse() {}
        public EnrollmentResponse(Long studentId, int enrolledImages, String message) {
            this.studentId = studentId; this.enrolledImages = enrolledImages; this.message = message;
        }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public int getEnrolledImages() { return enrolledImages; }
        public void setEnrolledImages(int v) { this.enrolledImages = v; }
        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
    }

    public static class TrainingRequest {
        private Long batchId;
        private List<Long> studentIds;

        public TrainingRequest() {}
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public List<Long> getStudentIds() { return studentIds; }
        public void setStudentIds(List<Long> v) { this.studentIds = v; }
    }

    public static class TrainingResponse {
        private Long batchId;
        private int trainedSamples;
        private int uniqueStudents;
        private String message;

        public TrainingResponse() {}
        public TrainingResponse(Long batchId, int trainedSamples, int uniqueStudents, String message) {
            this.batchId = batchId; this.trainedSamples = trainedSamples;
            this.uniqueStudents = uniqueStudents; this.message = message;
        }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public int getTrainedSamples() { return trainedSamples; }
        public void setTrainedSamples(int v) { this.trainedSamples = v; }
        public int getUniqueStudents() { return uniqueStudents; }
        public void setUniqueStudents(int v) { this.uniqueStudents = v; }
        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
    }

    public static class RecognitionRequest {
        private Long batchId;
        private Long subjectId;
        private Long facultyId;
        private String sessionToken;
        private String frameBase64;

        public RecognitionRequest() {}
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public String getSessionToken() { return sessionToken; }
        public void setSessionToken(String v) { this.sessionToken = v; }
        public String getFrameBase64() { return frameBase64; }
        public void setFrameBase64(String v) { this.frameBase64 = v; }
    }

    public static class RecognitionResult {
        private boolean recognized;
        private Long studentId;
        private Double confidenceScore;
        private String message;

        public RecognitionResult() {}
        public RecognitionResult(boolean recognized, Long studentId, Double confidenceScore, String message) {
            this.recognized = recognized; this.studentId = studentId;
            this.confidenceScore = confidenceScore; this.message = message;
        }
        public boolean isRecognized() { return recognized; }
        public void setRecognized(boolean v) { this.recognized = v; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double v) { this.confidenceScore = v; }
        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
    }
}
