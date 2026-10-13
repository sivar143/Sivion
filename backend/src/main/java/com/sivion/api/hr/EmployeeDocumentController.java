package com.sivion.api.hr;

import com.sivion.api.hr.domain.EmployeeDocument;
import java.time.Instant;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/hr/documents")
public class EmployeeDocumentController {
    private final EmployeeDocumentService service;
    private final EmployeeDocumentStorageService storage;

    public EmployeeDocumentController(EmployeeDocumentService service, EmployeeDocumentStorageService storage) {
        this.service = service;
        this.storage = storage;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public List<DocumentResponse> list(Authentication authentication) {
        return service.list(authentication).stream().map(DocumentResponse::from).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER')")
    public DocumentResponse upload(
        @RequestParam Long employeeId,
        @RequestParam(required = false) String description,
        @RequestParam("file") MultipartFile file,
        Authentication authentication
    ) {
        return DocumentResponse.from(service.upload(employeeId, description, file, authentication));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','HR_ADMIN','HR_USER','MANAGER','EMPLOYEE')")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication authentication) {
        EmployeeDocument document = service.get(id, authentication);
        Resource resource = storage.load(document.getStoredName());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(document.getOriginalName())
                .build()
                .toString())
            .header("X-Content-Type-Options", "nosniff")
            .body(resource);
    }

    public record DocumentResponse(
        Long id,
        Long employeeId,
        String originalName,
        String contentType,
        long fileSize,
        String description,
        String uploadedBy,
        Instant createdAt
    ) {
        static DocumentResponse from(EmployeeDocument document) {
            return new DocumentResponse(
                document.getId(), document.getEmployeeId(), document.getOriginalName(),
                document.getContentType(), document.getFileSize(), document.getDescription(),
                document.getUploadedBy(), document.getCreatedAt()
            );
        }
    }
}
