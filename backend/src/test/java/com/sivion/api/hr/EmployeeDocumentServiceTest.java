package com.sivion.api.hr;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.sivion.api.hr.domain.Employee;
import com.sivion.api.hr.repository.EmployeeDocumentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class EmployeeDocumentServiceTest {
    @Mock
    private EmployeeDocumentRepository repository;
    @Mock
    private EmployeeDocumentStorageService storage;
    @Mock
    private HrService hrService;
    @InjectMocks
    private EmployeeDocumentService service;

    @Test
    void employeeCannotUploadDocuments() {
        var auth = new UsernamePasswordAuthenticationToken("employee", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        assertThrows(AccessDeniedException.class, () -> service.upload(1L, "ID proof",
            new MockMultipartFile("file", "proof.pdf", "application/pdf", new byte[] { 1 }), auth));
    }

    @Test
    void employeeCannotReadAnotherEmployeesDocuments() {
        var auth = new UsernamePasswordAuthenticationToken("employee", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        when(repository.findByIdAndTenantId(9L, 1L)).thenReturn(java.util.Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.get(9L, auth));
    }
}
