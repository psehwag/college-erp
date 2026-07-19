package com.erp.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_role", length = 20)
    private String recipientRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "reference_id")
    private Long referenceId;   // studentId, subjectId, etc.

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "email_sent")
    private Boolean emailSent = false;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        ATTENDANCE_SHORTFALL, MARKS_UPLOADED, EXAM_SCHEDULE,
        GENERAL_ANNOUNCEMENT, DEFAULTER_WARNING, TIMETABLE_CHANGE,
        FEE_REMINDER, RESULT_PUBLISHED, FACE_ENROLLMENT_REQUIRED
    }
}
