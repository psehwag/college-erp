package com.erp.notification.controller;

import com.erp.notification.dto.NotificationDto;
import com.erp.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app and email notification endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send a direct notification (admin use)")
    public ResponseEntity<NotificationDto.ApiResponse<NotificationDto.Response>> send(
            @Valid @RequestBody NotificationDto.SendRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificationDto.ApiResponse.success("Notification sent",
                        notificationService.sendDirect(req)));
    }

    @GetMapping
    @Operation(summary = "Get notifications for current user")
    public ResponseEntity<NotificationDto.ApiResponse<Page<NotificationDto.Response>>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(NotificationDto.ApiResponse.success("Notifications fetched",
                notificationService.getNotifications(userId, page, size)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<NotificationDto.ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(NotificationDto.ApiResponse.success("Unread count",
                notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationDto.ApiResponse<Void>> markAsRead(
            @PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(NotificationDto.ApiResponse.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<NotificationDto.ApiResponse<Void>> markAllRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(NotificationDto.ApiResponse.success("All marked as read", null));
    }
}
