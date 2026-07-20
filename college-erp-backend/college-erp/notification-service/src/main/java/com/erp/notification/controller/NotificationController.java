package com.erp.notification.controller;

import com.erp.notification.entity.Notification;
import com.erp.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Transactional
public class NotificationController {

    private final NotificationRepository notifRepo;

    public NotificationController(NotificationRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications =
                notifRepo.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data",    notifications.getContent());
        resp.put("total",   notifications.getTotalElements());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/unread-count")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        long count = notifRepo.countByRecipientIdAndIsReadFalse(userId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data",    count);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        notifRepo.markOneRead(id, userId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Marked as read");
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(
            @RequestHeader("X-User-Id") Long userId) {
        notifRepo.markAllRead(userId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "All marked as read");
        return ResponseEntity.ok(resp);
    }
}
