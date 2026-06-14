package com.ecomera.notification.dto;

public record NotificationEvent(
        String recipient,
        String subject,
        String body,
        String type,
        String sourceService
) {
}
