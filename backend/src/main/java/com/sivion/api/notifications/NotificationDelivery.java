package com.sivion.api.notifications;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_deliveries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_notification_delivery_event_recipient",
        columnNames = {"tenant_id", "event_key", "recipient_username"})
})
public class NotificationDelivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private Long tenantId;
    @Column(name = "event_key", nullable = false, length = 180) private String eventKey;
    @Column(name = "recipient_username", nullable = false, length = 120) private String recipientUsername;
    protected NotificationDelivery() { }
    public NotificationDelivery(Long tenantId, String eventKey, String recipientUsername) {
        this.tenantId=tenantId; this.eventKey=eventKey; this.recipientUsername=recipientUsername;
    }
}