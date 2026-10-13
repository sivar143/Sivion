package com.sivion.api.notifications;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Notification> inbox(Long tenantId, String recipientUsername) {
        return repository.findTop100ByTenantIdAndRecipientUsernameOrderByCreatedAtDesc(
            tenantId,
            recipientUsername
        );
    }

    @Transactional
    public Notification send(Long tenantId, String recipientUsername, String title, String message) {
        String safeRecipient = requireText(recipientUsername, "Recipient is required");
        String safeTitle = requireText(title, "Title is required");
        String safeMessage = requireText(message, "Message is required");
        if (safeTitle.length() > 160 || safeMessage.length() > 2000 || safeRecipient.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification fields exceed their allowed length");
        }
        return repository.save(new Notification(tenantId, safeRecipient, safeTitle, safeMessage));
    }

    @Transactional
    public Notification markRead(Long tenantId, String recipientUsername, Long notificationId) {
        Notification notification = repository.findByIdAndTenantIdAndRecipientUsername(
            notificationId,
            tenantId,
            recipientUsername
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return notification;
    }

    @Transactional
    public int markAllRead(Long tenantId, String recipientUsername) {
        List<Notification> unread = repository.findByTenantIdAndRecipientUsernameAndReadAtIsNull(
            tenantId,
            recipientUsername
        );
        unread.forEach(Notification::markRead);
        return unread.size();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
