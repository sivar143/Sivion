package com.sivion.api.notifications;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop100ByTenantIdAndRecipientUsernameOrderByCreatedAtDesc(
        Long tenantId,
        String recipientUsername
    );

    Optional<Notification> findByIdAndTenantIdAndRecipientUsername(
        Long id,
        Long tenantId,
        String recipientUsername
    );

    List<Notification> findByTenantIdAndRecipientUsernameAndReadAtIsNull(
        Long tenantId,
        String recipientUsername
    );
}
