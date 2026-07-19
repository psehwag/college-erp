package com.erp.notification.dto;

import com.erp.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

public class NotificationDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendRequest {
        @NotNull private Long recipientId;
        private String recipientEmail;
        private String recipientRole;
        @NotNull private Notification.NotificationType type;
        @NotBlank private String title;
        @NotBlank private String message;
        private Long referenceId;
        private String referenceType;
        private boolean sendEmail;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private Long recipientId;
        private Notification.NotificationType type;
        private String title;
        private String message;
        private Long referenceId;
        private String referenceType;
        private Boolean isRead;
        private Boolean emailSent;
        private LocalDateTime createdAt;
    }

    @Data @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        public static <T> ApiResponse<T> success(String m, T d) {
            return ApiResponse.<T>builder().success(true).message(m).data(d).build();
        }
    }
}
