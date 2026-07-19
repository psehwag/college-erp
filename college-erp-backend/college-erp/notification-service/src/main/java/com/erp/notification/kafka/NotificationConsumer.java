package com.erp.notification.kafka;

import com.erp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "attendance-events", groupId = "notification-group")
    public void handleAttendanceEvent(NotificationEvent event) {
        log.info("Received attendance event for recipient: {}", event.getRecipientId());
        notificationService.processEvent(event);
    }

    @KafkaListener(topics = "marks-events", groupId = "notification-group")
    public void handleMarksEvent(NotificationEvent event) {
        log.info("Received marks event for recipient: {}", event.getRecipientId());
        notificationService.processEvent(event);
    }

    @KafkaListener(topics = "general-events", groupId = "notification-group")
    public void handleGeneralEvent(NotificationEvent event) {
        log.info("Received general event: {}", event.getTitle());
        notificationService.processEvent(event);
    }
}
