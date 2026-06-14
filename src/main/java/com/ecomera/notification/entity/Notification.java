package com.ecomera.notification.entity;

import com.ecomera.notification.enums.NotificationStatus;
import com.ecomera.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String recipient;
    private String subject;
    private String sourceService;
    private String body;
    private NotificationType type;
    private NotificationStatus status;
    private Instant createdAt;
    private Instant sentAt;
    private String errorMessage;
}
