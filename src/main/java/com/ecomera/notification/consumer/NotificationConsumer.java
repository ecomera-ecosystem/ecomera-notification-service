package com.ecomera.notification.consumer;

import com.ecomera.notification.dto.NotificationEvent;
import com.ecomera.notification.enums.NotificationType;
import com.ecomera.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/*
 * The CONSUMER is the bridge between Kafka and the notification service.
 *
 * How it fits in the architecture:
 *   Order Service  ──Kafka──→  Notification Consumer  ──→  Notification Service  ──→  MongoDB + Email
 *   Payment Service ──Kafka──→  (same consumer)        ──→  (same service)       ──→  (same)
 *
 * There's only ONE consumer. Both Order and Payment services send to the
 * SAME Kafka topic ("ecomera.notifications") and this consumer picks up both.
 *
 * @KafkaListener means Spring Kafka will automatically:
 *   - Subscribe to the topic on startup
 *   - Poll for new messages
 *   - Call this method when a message arrives
 *   - Commit the offset (mark as processed)
 */
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ecomera.notifications", groupId = "notification-service-group")
    public void consume(String message) {
        log.info("Received Kafka message: {}", message);

        try {

            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);

            NotificationType type = NotificationType.valueOf(event.type());

            notificationService.createAndSend(
                    event.recipient(),
                    event.subject(),
                    event.body(),
                    type,
                    event.sourceService()
            );

        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
        }
    }
}
