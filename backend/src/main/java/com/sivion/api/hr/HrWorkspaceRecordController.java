package com.sivion.api.hr;

import com.sivion.api.hr.domain.HrWorkspaceRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/hr/workspace-records")
public class HrWorkspaceRecordController {
    private final HrWorkspaceRecordService service;

    public HrWorkspaceRecordController(HrWorkspaceRecordService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<WorkspaceRecordResponse> list(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type,
        Authentication authentication
    ) {
        return service.list(tenantId, type, authentication).stream().map(WorkspaceRecordResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public WorkspaceRecordResponse create(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type,
        @Valid @RequestBody WorkspaceRecordRequest request,
        Authentication authentication
    ) {
        return WorkspaceRecordResponse.from(service.create(tenantId, authentication.getName(), type, toEntity(request), authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER')")
    public WorkspaceRecordResponse update(
        @RequestHeader(name = "X-Tenant-ID", defaultValue = "1") Long tenantId,
        @RequestParam String type,
        @PathVariable Long id,
        @Valid @RequestBody WorkspaceRecordRequest request,
        Authentication authentication
    ) {
        return WorkspaceRecordResponse.from(service.update(tenantId, id, type, toEntity(request), authentication));
    }

    private HrWorkspaceRecord toEntity(WorkspaceRecordRequest request) {
        HrWorkspaceRecord record = new HrWorkspaceRecord();
        record.setEmployeeId(request.employeeId());
        record.setTitle(request.title());
        record.setDescription(request.description());
        record.setScore(request.score());
        record.setResourceUrl(request.resourceUrl());
        record.setDueDate(request.dueDate());
        record.setStatus(request.status());
        return record;
    }

    public record WorkspaceRecordRequest(
        @NotBlank String title,
        Long employeeId,
        String description,
        Integer score,
        String resourceUrl,
        LocalDate dueDate,
        String status
    ) {}

    public record WorkspaceRecordResponse(
        Long id,
        String type,
        Long employeeId,
        String title,
        String description,
        Integer score,
        String resourceUrl,
        LocalDate dueDate,
        String status,
        String createdBy,
        Instant createdAt
    ) {
        static WorkspaceRecordResponse from(HrWorkspaceRecord record) {
            return new WorkspaceRecordResponse(
                record.getId(), record.getType(), record.getEmployeeId(), record.getTitle(),
                record.getDescription(), record.getScore(), record.getResourceUrl(), record.getDueDate(),
                record.getStatus(), record.getCreatedBy(), record.getCreatedAt()
            );
        }
    }
}
