package com.sivion.api.hr;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.domain.EmployeeDocument;
import com.sivion.api.hr.repository.EmployeeDocumentRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmployeeDocumentService {
    private static final Long TENANT_ID = 1L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final EmployeeDocumentRepository repository;
    private final EmployeeDocumentStorageService storage;
    private final HrService hrService;

    public EmployeeDocumentService(
        EmployeeDocumentRepository repository,
        EmployeeDocumentStorageService storage,
        HrService hrService
    ) {
        this.repository = repository;
        this.storage = storage;
        this.hrService = hrService;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocument> list(Authentication authentication) {
        List<Long> visibleIds = hrService.employees(authentication).stream()
            .map(Employee::getId).toList();
        if (visibleIds.isEmpty()) return List.of();
        return repository.findByTenantIdAndEmployeeIdInOrderByCreatedAtDesc(TENANT_ID, visibleIds);
    }

    public EmployeeDocument upload(
        Long employeeId,
        String description,
        MultipartFile file,
        Authentication authentication
    ) {
        requireHrUploader(authentication);
        requireVisibleEmployee(employeeId, authentication);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a document file");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Upload a PDF, JPG, PNG, DOC, DOCX or XLSX document");
        }
        if (description != null && description.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 1000 characters or fewer");
        }

        String originalName = file.getOriginalFilename() == null
            ? "document"
            : java.nio.file.Path.of(file.getOriginalFilename()).getFileName().toString();
        String storedName = storage.store(file);
        try {
            EmployeeDocument document = new EmployeeDocument();
            document.setTenantId(TENANT_ID);
            document.setEmployeeId(employeeId);
            document.setOriginalName(originalName.length() > 255 ? originalName.substring(0, 255) : originalName);
            document.setStoredName(storedName);
            document.setContentType(contentType);
            document.setFileSize(file.getSize());
            document.setDescription(description == null ? null : description.trim());
            document.setUploadedBy(authentication.getName());
            return repository.save(document);
        } catch (RuntimeException exception) {
            storage.deleteQuietly(storedName);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public EmployeeDocument get(Long id, Authentication authentication) {
        EmployeeDocument document = repository.findByIdAndTenantId(id, TENANT_ID)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        requireVisibleEmployee(document.getEmployeeId(), authentication);
        return document;
    }

    private void requireVisibleEmployee(Long employeeId, Authentication authentication) {
        boolean visible = hrService.employees(authentication).stream()
            .anyMatch(employee -> employeeId.equals(employee.getId()));
        if (!visible) throw new AccessDeniedException("You are not allowed to access this employee's documents");
    }

    private void requireHrUploader(Authentication authentication) {
        boolean allowed = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .anyMatch(Set.of("ROLE_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_USER")::contains);
        if (!allowed) throw new AccessDeniedException("Only HR or administrators can upload employee documents");
    }
}
