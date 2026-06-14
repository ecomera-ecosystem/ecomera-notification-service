package com.ecomera.notification.controller;

import com.ecomera.notification.entity.Notification;
import com.ecomera.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a notification")
    @ApiResponse(responseCode = "201", description = "Notification created")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.create(notification));
    }

    @GetMapping
    @Operation(summary = "Get current user's notifications")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved")
    public ResponseEntity<List<Notification>> getAll(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(notificationService.getByRecipient(email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    @ApiResponse(responseCode = "200", description = "Notification retrieved")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Notification> getById(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a notification")
    @ApiResponse(responseCode = "200", description = "Notification updated")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Notification> update(@PathVariable String id, @RequestBody Notification update) {
        return ResponseEntity.ok(notificationService.update(id, update));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    @ApiResponse(responseCode = "204", description = "Notification deleted")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
