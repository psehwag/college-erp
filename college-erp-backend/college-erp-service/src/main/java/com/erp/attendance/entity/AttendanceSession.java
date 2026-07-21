package com.erp.attendance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_id", nullable = false)
    private Long facultyId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "session_token", unique = true, length = 64)
    private String sessionToken;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AttendanceSession() {}

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
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus v) { this.status = v; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String v) { this.sessionToken = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum SessionStatus { ACTIVE, COMPLETED, CANCELLED }
}
