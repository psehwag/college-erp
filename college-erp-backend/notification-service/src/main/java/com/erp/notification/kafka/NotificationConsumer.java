package com.erp.notification.kafka;

import com.erp.notification.entity.Notification;
import com.erp.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.logging.Logger;

@Component
@Transactional
public class NotificationConsumer {

    private static final Logger log = Logger.getLogger(NotificationConsumer.class.getName());

    private final NotificationRepository notifRepo;
    private final JavaMailSender         mailSender;

    public NotificationConsumer(NotificationRepository notifRepo, JavaMailSender mailSender) {
        this.notifRepo  = notifRepo;
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "attendance-events", groupId = "notification-group")
    public void handleAttendance(Map<String, Object> event) {
        log.info("Attendance event received");
        processEvent(event);
    }

    @KafkaListener(topics = "marks-events", groupId = "notification-group")
    public void handleMarks(Map<String, Object> event) {
        log.info("Marks event received");
        processEvent(event);
    }

    @KafkaListener(topics = "general-events", groupId = "notification-group")
    public void handleGeneral(Map<String, Object> event) {
        log.info("General event received: " + event.get("title"));
        processEvent(event);
    }

    private void processEvent(Map<String, Object> event) {
        try {
            Notification n = new Notification();

            Object recipientId = event.get("recipientId");
            if (recipientId != null) n.setRecipientId(Long.parseLong(recipientId.toString()));

            n.setRecipientEmail(event.get("recipientEmail") != null
                    ? event.get("recipientEmail").toString() : null);
            n.setRecipientRole(event.get("recipientRole") != null
                    ? event.get("recipientRole").toString() : null);

            String typeStr = event.getOrDefault("type", "GENERAL_ANNOUNCEMENT").toString();
            try { n.setType(Notification.NotificationType.valueOf(typeStr)); }
            catch (Exception e) { n.setType(Notification.NotificationType.GENERAL_ANNOUNCEMENT); }

            n.setTitle(event.getOrDefault("title", "Notification").toString());
            n.setMessage(event.getOrDefault("message", "").toString());
            n.setIsRead(false);
            n.setEmailSent(false);

            n = notifRepo.save(n);

            boolean sendEmail = Boolean.parseBoolean(
                    event.getOrDefault("sendEmail", "false").toString());

            if (sendEmail && n.getRecipientEmail() != null) {
                sendEmail(n.getRecipientEmail(), n.getTitle(), n.getMessage());
                n.setEmailSent(true);
                notifRepo.save(n);
            }

        } catch (Exception e) {
            log.severe("Error processing notification event: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject("[College ERP] " + subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to: " + to);
        } catch (Exception e) {
            log.warning("Email send failed to " + to + ": " + e.getMessage());
        }
    }
}
