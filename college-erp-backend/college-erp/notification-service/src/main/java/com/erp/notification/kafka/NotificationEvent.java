package com.erp.notification.kafka;

import com.erp.notification.entity.Notification;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEvent {
    private Long recipientId;
    private String recipientEmail;
    private String recipientRole;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private boolean sendEmail;
}
