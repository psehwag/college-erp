package com.erp.attendance.dto;

import com.erp.attendance.entity.Attendance;
import com.erp.attendance.entity.AttendanceSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AttendanceDto {

    // ── Bulk mark request ─────────────────────────────────────────────────

    public static class BulkMarkRequest {
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private LocalDate attendanceDate;
        private List<StudentMark> studentMarks;

        public BulkMarkRequest() {}
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public LocalDate getAttendanceDate() { return attendanceDate; }
        public void setAttendanceDate(LocalDate v) { this.attendanceDate = v; }
        public List<StudentMark> getStudentMarks() { return studentMarks; }
        public void setStudentMarks(List<StudentMark> v) { this.studentMarks = v; }
    }

    public static class StudentMark {
        private Long studentId;
        private Attendance.AttendanceStatus status;
        private String remarks;

        public StudentMark() {}
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public Attendance.AttendanceStatus getStatus() { return status; }
        public void setStatus(Attendance.AttendanceStatus v) { this.status = v; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String v) { this.remarks = v; }
    }

    // ── Face recognition mark ─────────────────────────────────────────────

    public static class FaceMarkRequest {
        private Long studentId;
        private Long subjectId;
        private Long facultyId;
        private Long batchId;
        private String sessionToken;
        private Double confidenceScore;

        public FaceMarkRequest() {}
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public String getSessionToken() { return sessionToken; }
        public void setSessionToken(String v) { this.sessionToken = v; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double v) { this.confidenceScore = v; }
    }

    // ── Session request ───────────────────────────────────────────────────

    public static class StartSessionRequest {
        private Long facultyId;
        private Long subjectId;
        private Long batchId;

        public StartSessionRequest() {}
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
    }

    // ── Response ──────────────────────────────────────────────────────────

    public static class Response {
        private Long id;
        private Long studentId;
        private Long subjectId;
        private Long facultyId;
        private Long batchId;
        private LocalDate attendanceDate;
        private LocalTime checkInTime;
        private Attendance.AttendanceStatus status;
        private Attendance.MarkedBy markedBy;
        private Double confidenceScore;
        private String remarks;
        private LocalDateTime createdAt;

        public Response() {}
        public Long getId() { return id; }
        public void setId(Long v) { this.id = v; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public LocalDate getAttendanceDate() { return attendanceDate; }
        public void setAttendanceDate(LocalDate v) { this.attendanceDate = v; }
        public LocalTime getCheckInTime() { return checkInTime; }
        public void setCheckInTime(LocalTime v) { this.checkInTime = v; }
        public Attendance.AttendanceStatus getStatus() { return status; }
        public void setStatus(Attendance.AttendanceStatus v) { this.status = v; }
        public Attendance.MarkedBy getMarkedBy() { return markedBy; }
        public void setMarkedBy(Attendance.MarkedBy v) { this.markedBy = v; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double v) { this.confidenceScore = v; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String v) { this.remarks = v; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    }

    // ── Percentage response ───────────────────────────────────────────────

    public static class PercentageResponse {
        private Long studentId;
        private Long subjectId;
        private long totalClasses;
        private long presentClasses;
        private double percentage;
        private boolean isShortfall;

        public PercentageResponse() {}
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long v) { this.studentId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public long getTotalClasses() { return totalClasses; }
        public void setTotalClasses(long v) { this.totalClasses = v; }
        public long getPresentClasses() { return presentClasses; }
        public void setPresentClasses(long v) { this.presentClasses = v; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double v) { this.percentage = v; }
        public boolean isShortfall() { return isShortfall; }
        public void setShortfall(boolean v) { this.isShortfall = v; }
    }

    // ── Session response ──────────────────────────────────────────────────

    public static class SessionResponse {
        private Long id;
        private Long facultyId;
        private Long subjectId;
        private Long batchId;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AttendanceSession.SessionStatus status;
        private String sessionToken;

        public SessionResponse() {}
        public Long getId() { return id; }
        public void setId(Long v) { this.id = v; }
        public Long getFacultyId() { return facultyId; }
        public void setFacultyId(Long v) { this.facultyId = v; }
        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long v) { this.subjectId = v; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long v) { this.batchId = v; }
        public LocalDate getSessionDate() { return sessionDate; }
        public void setSessionDate(LocalDate v) { this.sessionDate = v; }
        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime v) { this.startTime = v; }
        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime v) { this.endTime = v; }
        public AttendanceSession.SessionStatus getStatus() { return status; }
        public void setStatus(AttendanceSession.SessionStatus v) { this.status = v; }
        public String getSessionToken() { return sessionToken; }
        public void setSessionToken(String v) { this.sessionToken = v; }
    }
}
