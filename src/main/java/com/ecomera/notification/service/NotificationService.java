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

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

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
