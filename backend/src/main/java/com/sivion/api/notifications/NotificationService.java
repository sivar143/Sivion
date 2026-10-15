package com.sivion.api.notifications;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationDeliveryRepository deliveries;

    @Autowired
    public NotificationService(NotificationRepository repository, NotificationDeliveryRepository deliveries) {
        this.repository = repository;
        this.deliveries = deliveries;
    }

    /** Compatibility constructor for service unit tests that do not exercise event delivery deduplication. */
    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
        this.deliveries = null;
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
    public Notification sendOnce(Long tenantId, String eventKey, String recipientUsername,
            String title, String message) {
        String safeEventKey = requireText(eventKey, "Event key is required");
        if (safeEventKey.length() > 180) safeEventKey = safeEventKey.substring(0, 180);
        String safeRecipient = requireText(recipientUsername, "Recipient is required");
        if (deliveries != null && deliveries.existsByTenantIdAndEventKeyAndRecipientUsername(
                tenantId, safeEventKey, safeRecipient)) {
            return null;
        }
        Notification notification = send(tenantId, safeRecipient, title, message);
        if (deliveries != null) deliveries.save(new NotificationDelivery(tenantId, safeEventKey, safeRecipient));
        return notification;
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
