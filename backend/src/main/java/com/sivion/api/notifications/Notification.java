package com.sivion.api.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "user_notifications",
    indexes = @Index(
        name = "idx_notification_inbox",
        columnList = "tenant_id, recipient_username, created_at"
    )
)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "recipient_username", nullable = false, length = 120)
    private String recipientUsername;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
    }

    public Notification(Long tenantId, String recipientUsername, String title, String message) {
        this.tenantId = tenantId;
        this.recipientUsername = recipientUsername;
        this.title = title;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getRecipientUsername() { return recipientUsername; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
    public void markRead() { if (readAt == null) readAt = Instant.now(); }
}
