package com.sivion.api.notifications;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
    boolean existsByTenantIdAndEventKeyAndRecipientUsername(Long tenantId, String eventKey, String recipientUsername);
}