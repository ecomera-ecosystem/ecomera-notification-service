package com.ecomera.notification.repository;

import com.ecomera.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
}
