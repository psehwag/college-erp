package com.erp.attendance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"student_id", "subject_id", "attendance_date"}))
public class Attendance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "marked_by")
    private MarkedBy markedBy;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Attendance() {}

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
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus v) { this.status = v; }
    public MarkedBy getMarkedBy() { return markedBy; }
    public void setMarkedBy(MarkedBy v) { this.markedBy = v; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double v) { this.confidenceScore = v; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String v) { this.remarks = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }
    public enum MarkedBy { MANUAL, FACE_RECOGNITION }
}
