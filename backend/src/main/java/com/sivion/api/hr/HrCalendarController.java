package com.sivion.api.hr;

import com.sivion.api.hr.domain.HrCalendarEntry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hr/calendar")
public class HrCalendarController {
    private final HrCalendarService service;

    public HrCalendarController(HrCalendarService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<CalendarEntryResponse> list(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type
    ) {
        return service.list(tenantId, type).stream().map(CalendarEntryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public CalendarEntryResponse create(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type,
        @Valid @RequestBody CalendarEntryRequest request,
        Authentication authentication
    ) {
        HrCalendarEntry entry = toEntity(request);
        return CalendarEntryResponse.from(service.create(tenantId, authentication.getName(), type, entry));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public CalendarEntryResponse update(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type,
        @PathVariable Long id,
        @Valid @RequestBody CalendarEntryRequest request
    ) {
        return CalendarEntryResponse.from(service.update(tenantId, id, type, toEntity(request)));
    }

    private HrCalendarEntry toEntity(CalendarEntryRequest request) {
        HrCalendarEntry entry = new HrCalendarEntry();
        entry.setTitle(request.title());
        entry.setDescription(request.description());
        entry.setStartsOn(request.startsOn());
        entry.setEndsOn(request.endsOn());
        entry.setStatus(request.status());
        return entry;
    }

    public record CalendarEntryRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDate startsOn,
        @NotNull LocalDate endsOn,
        String status
    ) {}

    public record CalendarEntryResponse(
        Long id,
        String type,
        String title,
        String description,
        LocalDate startsOn,
        LocalDate endsOn,
        String status,
        String createdBy,
        Instant createdAt
    ) {
        static CalendarEntryResponse from(HrCalendarEntry entry) {
            return new CalendarEntryResponse(
                entry.getId(), entry.getType(), entry.getTitle(), entry.getDescription(),
                entry.getStartsOn(), entry.getEndsOn(), entry.getStatus(),
                entry.getCreatedBy(), entry.getCreatedAt()
            );
        }
    }
}
