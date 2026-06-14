package com.ecomera.notification.controller;

import com.ecomera.notification.entity.Notification;
import com.ecomera.notification.enums.NotificationStatus;
import com.ecomera.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        notification.setId(null);
        notification.setCreatedAt(Instant.now());
        notification.setSentAt(null);
        notification.setErrorMessage(null);
        if (notification.getStatus() == null) {
            notification.setStatus(NotificationStatus.PENDING);
        }
        Notification saved = repository.save(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> update(@PathVariable String id, @RequestBody Notification update) {
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
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
