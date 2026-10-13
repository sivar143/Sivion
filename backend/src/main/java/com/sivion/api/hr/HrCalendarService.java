package com.sivion.api.hr;

import com.sivion.api.hr.domain.HrCalendarEntry;
import com.sivion.api.hr.repository.HrCalendarEntryRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class HrCalendarService {
    private static final Set<String> TYPES = Set.of("HOLIDAY", "MEETING");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "CANCELLED");
    private final HrCalendarEntryRepository repository;

    public HrCalendarService(HrCalendarEntryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HrCalendarEntry> list(Long tenantId, String type) {
        return repository.findByTenantIdAndTypeOrderByStartsOnAsc(tenantId, normalizeType(type));
    }

    public HrCalendarEntry create(Long tenantId, String actor, String type, HrCalendarEntry input) {
        validate(input, type);
        input.setTenantId(tenantId);
        input.setType(normalizeType(type));
        input.setTitle(input.getTitle().trim());
        input.setDescription(input.getDescription() == null ? null : input.getDescription().trim());
        input.setStatus(normalizeStatus(input.getStatus()));
        input.createdBy = actor;
        return repository.save(input);
    }

    public HrCalendarEntry update(Long tenantId, Long id, String type, HrCalendarEntry input) {
        HrCalendarEntry current = repository.findByIdAndTenantId(id, tenantId)
            .filter(existing -> existing.getType().equals(normalizeType(type)))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendar entry not found"));
        validate(input, type);
        current.setTitle(input.getTitle().trim());
        current.setDescription(input.getDescription() == null ? null : input.getDescription().trim());
        current.setStartsOn(input.getStartsOn());
        current.setEndsOn(input.getEndsOn());
        current.setStatus(normalizeStatus(input.getStatus()));
        return repository.save(current);
    }

    private void validate(HrCalendarEntry entry, String type) {
        normalizeType(type);
        if (entry.getTitle() == null || entry.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (entry.getTitle().trim().length() > 160) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be 160 characters or fewer");
        }
        if (entry.getStartsOn() == null || entry.getEndsOn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
        }
        if (entry.getEndsOn().isBefore(entry.getStartsOn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        }
        if (entry.getDescription() != null && entry.getDescription().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 2000 characters or fewer");
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported calendar entry type");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank()
            ? "ACTIVE"
            : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or CANCELLED");
        }
        return normalized;
    }
}
