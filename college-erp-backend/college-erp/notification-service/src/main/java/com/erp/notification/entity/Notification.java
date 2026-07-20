package com.erp.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id")     private Long   recipientId;
    @Column(name = "recipient_email")  private String recipientEmail;
    @Column(name = "recipient_role", length = 20) private String recipientRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, length = 2000) private String message;

    @Column(name = "reference_id")    private Long   referenceId;
    @Column(name = "reference_type", length = 50) private String referenceType;

    @Column(name = "is_read")   private Boolean isRead   = false;
    @Column(name = "email_sent") private Boolean emailSent = false;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long v) { this.recipientId = v; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String v) { this.recipientEmail = v; }
    public String getRecipientRole() { return recipientRole; }
    public void setRecipientRole(String v) { this.recipientRole = v; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType v) { this.type = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long v) { this.referenceId = v; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String v) { this.referenceType = v; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean v) { this.isRead = v; }
    public Boolean getEmailSent() { return emailSent; }
    public void setEmailSent(Boolean v) { this.emailSent = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public enum NotificationType {
        ATTENDANCE_SHORTFALL, MARKS_UPLOADED, EXAM_SCHEDULE,
        GENERAL_ANNOUNCEMENT, DEFAULTER_WARNING, RESULT_PUBLISHED,
        FACE_ENROLLMENT_REQUIRED, TIMETABLE_CHANGE, FEE_REMINDER
    }
}
