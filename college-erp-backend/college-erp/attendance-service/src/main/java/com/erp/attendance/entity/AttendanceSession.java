package com.erp.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "session_token", unique = true, length = 64)
    private String sessionToken; // For face recognition session

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SessionStatus { ACTIVE, COMPLETED, CANCELLED }
}
