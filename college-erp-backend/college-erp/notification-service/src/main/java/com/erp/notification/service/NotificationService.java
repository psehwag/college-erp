package com.erp.notification.service;

import com.erp.notification.dto.NotificationDto;
import com.erp.notification.entity.Notification;
import com.erp.notification.kafka.NotificationEvent;
import com.erp.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j @Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public void processEvent(NotificationEvent event) {
        Notification notification = Notification.builder()
                .recipientId(event.getRecipientId())
                .recipientEmail(event.getRecipientEmail())
                .recipientRole(event.getRecipientRole())
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .referenceId(event.getReferenceId())
                .referenceType(event.getReferenceType())
                .build();

        notification = notificationRepository.save(notification);

        if (event.isSendEmail() && event.getRecipientEmail() != null) {
            sendEmail(event.getRecipientEmail(), event.getTitle(), event.getMessage());
            notification.setEmailSent(true);
            notificationRepository.save(notification);
        }
    }

    public NotificationDto.Response sendDirect(NotificationDto.SendRequest req) {
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(req.getRecipientId()).recipientEmail(req.getRecipientEmail())
                .recipientRole(req.getRecipientRole()).type(req.getType())
                .title(req.getTitle()).message(req.getMessage())
                .referenceId(req.getReferenceId()).referenceType(req.getReferenceType())
                .sendEmail(req.isSendEmail()).build();
        processEvent(event);

        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(req.getRecipientId(), PageRequest.of(0, 1))
                .getContent().stream().map(this::toResponse).findFirst()
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> getNotifications(Long recipientId, int page, int size) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    public void markAsRead(Long notificationId, Long recipientId) {
        notificationRepository.markAsRead(notificationId, recipientId);
    }

    public void markAllRead(Long recipientId) {
        notificationRepository.markAllReadByRecipient(recipientId);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[College ERP] " + subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private NotificationDto.Response toResponse(Notification n) {
        return NotificationDto.Response.builder()
                .id(n.getId()).recipientId(n.getRecipientId())
                .type(n.getType()).title(n.getTitle()).message(n.getMessage())
                .referenceId(n.getReferenceId()).referenceType(n.getReferenceType())
                .isRead(n.getIsRead()).emailSent(n.getEmailSent()).createdAt(n.getCreatedAt()).build();
    }
}
