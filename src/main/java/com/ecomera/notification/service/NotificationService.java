package com.ecomera.notification.service;

import com.ecomera.notification.entity.Notification;
import com.ecomera.notification.enums.NotificationStatus;
import com.ecomera.notification.enums.NotificationType;
import com.ecomera.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public List<Notification> getByRecipient(String email) {
        return repository.findByRecipientOrderByCreatedAtDesc(email);
    }

    public Notification getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public Notification create(Notification notification) {
        notification.setId(null);
        notification.setCreatedAt(Instant.now());
        notification.setSentAt(null);
        notification.setErrorMessage(null);
        if (notification.getStatus() == null) {
            notification.setStatus(NotificationStatus.PENDING);
        }
        return repository.save(notification);
    }

    public Notification update(String id, Notification update) {
        return repository.findById(id)
                .map(existing -> {
                    if (update.getRecipient() != null) existing.setRecipient(update.getRecipient());
                    if (update.getSubject() != null) existing.setSubject(update.getSubject());
                    if (update.getBody() != null) existing.setBody(update.getBody());
                    if (update.getType() != null) existing.setType(update.getType());
                    if (update.getStatus() != null) existing.setStatus(update.getStatus());
                    if (update.getSourceService() != null) existing.setSourceService(update.getSourceService());
                    if (update.getSentAt() != null) existing.setSentAt(update.getSentAt());
                    if (update.getErrorMessage() != null) existing.setErrorMessage(update.getErrorMessage());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public Notification markAsRead(String id) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setStatus(NotificationStatus.READ);
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Notification not found: " + id);
        }
        repository.deleteById(id);
    }

    public Notification createAndSend(String recipient, String subject, String body,
                                      NotificationType type, String sourceService) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .type(type)
                .sourceService(sourceService)
                .status(NotificationStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Save before sending — if email fails, we keep a record
        Notification saved = repository.save(notification);
        log.info("Notification {} saved with status PENDING", saved.getId());

        // Email is a side effect; failure is recorded but not propagated
        try {
            sendEmail(saved);
            saved.setStatus(NotificationStatus.SENT);
            saved.setSentAt(Instant.now());
            log.info("Notification {} sent successfully", saved.getId());
        } catch (Exception e) {
            saved.setStatus(NotificationStatus.FAILED);
            saved.setErrorMessage(e.getMessage());
            log.error("Failed to send notification {}: {}", saved.getId(), e.getMessage());
        }

        // Persist final status (SENT or FAILED)
        return repository.save(saved);
    }

    private void sendEmail(Notification notification) throws MessagingException {
        Context context = new Context();
        context.setVariable("subject", notification.getSubject());
        context.setVariable("bodyContent", notification.getBody());

        String htmlContent = templateEngine.process("email/confirmation", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setTo(notification.getRecipient());
        helper.setSubject(notification.getSubject());
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }
}
