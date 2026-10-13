package com.sivion.api.marketing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Campaign> findByIdAndTenantId(Long id, Long tenantId);
}
