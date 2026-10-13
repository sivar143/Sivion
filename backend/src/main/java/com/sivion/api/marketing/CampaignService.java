package com.sivion.api.marketing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CampaignService {
    private static final Set<String> CHANNELS = Set.of("EMAIL", "SOCIAL", "SEARCH", "EVENT", "PARTNER", "OTHER");
    private static final Set<String> STATUSES = Set.of("DRAFT", "PLANNED", "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED");
    private final CampaignRepository repository;

    public CampaignService(CampaignRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Campaign> list(Long tenantId) {
        return repository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public Campaign create(Long tenantId, Campaign input) {
        validate(input);
        input.setTenantId(tenantId);
        input.setName(input.getName().trim());
        input.setChannel(input.getChannel().trim().toUpperCase(Locale.ROOT));
        input.setGoal(input.getGoal() == null ? null : input.getGoal().trim());
        input.setStatus(normalizeStatus(input.getStatus()));
        input.setBudget(normalizeBudget(input.getBudget()));
        return repository.save(input);
    }

    public Campaign update(Long tenantId, Long id, Campaign input) {
        Campaign current = repository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        validate(input);
        current.setName(input.getName().trim());
        current.setChannel(input.getChannel().trim().toUpperCase(Locale.ROOT));
        current.setBudget(normalizeBudget(input.getBudget()));
        current.setStartDate(input.getStartDate());
        current.setEndDate(input.getEndDate());
        current.setStatus(normalizeStatus(input.getStatus()));
        current.setGoal(input.getGoal() == null ? null : input.getGoal().trim());
        return repository.save(current);
    }

    private void validate(Campaign campaign) {
        if (campaign.getName() == null || campaign.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name is required");
        }
        if (campaign.getName().trim().length() > 160) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name must be 160 characters or fewer");
        }
        if (campaign.getChannel() == null || !CHANNELS.contains(campaign.getChannel().trim().toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a supported campaign channel");
        }
        if (campaign.getStartDate() != null && campaign.getEndDate() != null
                && campaign.getEndDate().isBefore(campaign.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        }
        if (campaign.getGoal() != null && campaign.getGoal().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign goal must be 1000 characters or fewer");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank()
            ? "DRAFT"
            : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a supported campaign status");
        }
        return normalized;
    }

    private BigDecimal normalizeBudget(BigDecimal budget) {
        BigDecimal value = budget == null ? BigDecimal.ZERO : budget;
        if (value.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign budget cannot be negative");
        }
        return value;
    }
}
