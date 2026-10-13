package com.sivion.api.notifications;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> inbox(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        Authentication authentication
    ) {
        return service.inbox(tenantId, authentication.getName()).stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @PathVariable Long id,
        Authentication authentication
    ) {
        return NotificationResponse.from(service.markRead(tenantId, authentication.getName(), id));
    }

    @PatchMapping("/read-all")
    public MarkAllReadResponse markAllRead(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        Authentication authentication
    ) {
        return new MarkAllReadResponse(service.markAllRead(tenantId, authentication.getName()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationResponse send(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @Valid @RequestBody CreateNotificationRequest request
    ) {
        return NotificationResponse.from(
            service.send(tenantId, request.recipientUsername(), request.title(), request.message())
        );
    }

    public record CreateNotificationRequest(
        @NotBlank @Size(max = 120) String recipientUsername,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 2000) String message
    ) {}

    public record MarkAllReadResponse(int updatedCount) {}

    public record NotificationResponse(
        Long id,
        String title,
        String message,
        Instant createdAt,
        Instant readAt
    ) {
        static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt()
            );
        }
    }
}
