package com.sivion.api.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AuditRecordResponse> list(
            @RequestHeader(name = "X-Tenant-ID", defaultValue = "unspecified") String tenantId,
            @RequestParam(defaultValue = "100") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return repository.findByTenantIdOrderByOccurredAtDesc(
                        tenantId,
                        PageRequest.of(0, safeLimit))
                .stream()
                .map(record -> new AuditRecordResponse(
                        record.getId(),
                        record.getActor(),
                        record.getMethod(),
                        record.getEndpoint(),
                        record.getStatusCode(),
                        record.getOccurredAt(),
                        record.getClientAddress()))
                .toList();
    }

    public record AuditRecordResponse(
            Long id,
            String actor,
            String method,
            String endpoint,
            int statusCode,
            Instant occurredAt,
            String clientAddress) {
    }
}
