package com.sivion.api.hr;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.HrWorkspaceRecord;
import com.sivion.api.hr.repository.HrWorkspaceRecordRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class HrWorkspaceRecordService {
    private static final Set<String> TYPES = Set.of("PERFORMANCE", "LEARNING");
    private static final Set<String> STATUSES = Set.of("DRAFT", "ACTIVE", "SUBMITTED", "REVIEWED", "COMPLETED", "ARCHIVED");
    private final HrWorkspaceRecordRepository repository;
    private final HrService hrService;

    public HrWorkspaceRecordService(HrWorkspaceRecordRepository repository, HrService hrService) {
        this.repository = repository;
        this.hrService = hrService;
    }

    @Transactional(readOnly = true)
    public List<HrWorkspaceRecord> list(Long tenantId, String type, Authentication authentication) {
        String normalizedType = normalizeType(type);
        List<HrWorkspaceRecord> records = repository.findByTenantIdAndTypeOrderByCreatedAtDesc(tenantId, normalizedType);
        if ("LEARNING".equals(normalizedType)) return records;
        Set<Long> visibleEmployeeIds = hrService.employees(authentication).stream()
            .map(Employee::getId)
            .collect(java.util.stream.Collectors.toSet());
        return records.stream()
            .filter(record -> record.getEmployeeId() != null && visibleEmployeeIds.contains(record.getEmployeeId()))
            .toList();
    }

    public HrWorkspaceRecord create(Long tenantId, String actor, String type, HrWorkspaceRecord input, Authentication authentication) {
        String normalizedType = normalizeType(type);
        validate(input, normalizedType);
        validatePermissions(normalizedType, authentication);
        if ("PERFORMANCE".equals(normalizedType)) {
            requireVisibleEmployee(input.getEmployeeId(), authentication);
        } else {
            input.setEmployeeId(null);
            input.setScore(null);
        }
        input.setTenantId(tenantId);
        input.setType(normalizedType);
        input.setTitle(input.getTitle().trim());
        input.setDescription(input.getDescription() == null ? null : input.getDescription().trim());
        input.setResourceUrl(normalizeUrl(input.getResourceUrl()));
        input.setStatus(normalizeStatus(input.getStatus()));
        input.setCreatedBy(actor);
        return repository.save(input);
    }

    public HrWorkspaceRecord update(Long tenantId, Long id, String type, HrWorkspaceRecord input, Authentication authentication) {
        String normalizedType = normalizeType(type);
        HrWorkspaceRecord current = repository.findByIdAndTenantId(id, tenantId)
            .filter(record -> record.getType().equals(normalizedType))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HR record not found"));
        validate(input, normalizedType);
        validatePermissions(normalizedType, authentication);
        if ("PERFORMANCE".equals(normalizedType)) {
            requireVisibleEmployee(input.getEmployeeId(), authentication);
        } else {
            input.setEmployeeId(null);
            input.setScore(null);
        }
        current.setEmployeeId(input.getEmployeeId());
        current.setTitle(input.getTitle().trim());
        current.setDescription(input.getDescription() == null ? null : input.getDescription().trim());
        current.setScore(input.getScore());
        current.setResourceUrl(normalizeUrl(input.getResourceUrl()));
        current.setDueDate(input.getDueDate());
        current.setStatus(normalizeStatus(input.getStatus()));
        return repository.save(current);
    }

    private void validatePermissions(String type, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
            .collect(java.util.stream.Collectors.toSet());
        if ("LEARNING".equals(type)) {
            if (!roles.stream().anyMatch(Set.of("ADMIN", "HR_ADMIN", "HR_USER", "MANAGER")::contains)) {
                throw new AccessDeniedException("You are not allowed to manage learning resources");
            }
        } else if (!roles.stream().anyMatch(Set.of("ADMIN", "HR_ADMIN", "HR_USER", "MANAGER")::contains)) {
            throw new AccessDeniedException("You are not allowed to manage performance reviews");
        }
    }

    private void requireVisibleEmployee(Long employeeId, Authentication authentication) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is required for a performance review");
        }
        boolean visible = hrService.employees(authentication).stream()
            .anyMatch(employee -> employeeId.equals(employee.getId()));
        if (!visible) {
            throw new AccessDeniedException("You are not allowed to access this employee's performance review");
        }
    }

    private void validate(HrWorkspaceRecord record, String type) {
        if (record.getTitle() == null || record.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (record.getTitle().trim().length() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be 180 characters or fewer");
        }
        if (record.getDescription() != null && record.getDescription().length() > 3000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 3000 characters or fewer");
        }
        if ("PERFORMANCE".equals(type)) {
            if (record.getScore() == null || record.getScore() < 1 || record.getScore() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Performance score must be between 1 and 5");
            }
        } else if (record.getResourceUrl() != null && record.getResourceUrl().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource URL must be 1000 characters or fewer");
        }
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported HR record type");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank() ? "DRAFT" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported HR record status");
        }
        return normalized;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String normalized = url.trim();
        if (!(normalized.startsWith("https://") || normalized.startsWith("/"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource links must use HTTPS or an application-relative path");
        }
        return normalized;
    }
}
