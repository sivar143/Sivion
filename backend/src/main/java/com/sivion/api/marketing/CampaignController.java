package com.sivion.api.marketing;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/marketing/campaigns")
@PreAuthorize("hasAnyRole('ADMIN','MARKETING_USER')")
public class CampaignController {
    private final CampaignService service;

    public CampaignController(CampaignService service) {
        this.service = service;
    }

    @GetMapping
    public List<Campaign> list(@RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId) {
        return service.list(tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Campaign create(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestBody Campaign campaign
    ) {
        return service.create(tenantId, campaign);
    }

    @PutMapping("/{id}")
    public Campaign update(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @PathVariable Long id,
        @RequestBody Campaign campaign
    ) {
        return service.update(tenantId, id, campaign);
    }
}
