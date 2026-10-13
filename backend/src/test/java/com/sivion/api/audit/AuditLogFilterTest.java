package com.sivion.api.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditLogFilterTest {

    private final AuditLogRepository repository = org.mockito.Mockito.mock(AuditLogRepository.class);
    private final AuditLogFilter filter = new AuditLogFilter(repository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recordsWriteRequestMetadataWithoutRequiringARequestBody() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("siva", "not-used", java.util.List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/hr/leaves");
        request.addHeader("X-Tenant-ID", "tenant-7");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(201);

        filter.doFilter(request, response, new MockFilterChain());

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(record ->
                "tenant-7".equals(record.getTenantId())
                        && "siva".equals(record.getActor())
                        && "POST".equals(record.getMethod())
                        && "/api/v1/hr/leaves".equals(record.getEndpoint())
                        && record.getStatusCode() == 201));
    }

    @Test
    void doesNotAuditReadRequestsOrTheAuditEndpoint() throws Exception {
        MockHttpServletRequest readRequest = new MockHttpServletRequest("GET", "/api/v1/hr/employees");
        filter.doFilter(readRequest, new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletRequest auditRequest = new MockHttpServletRequest("POST", "/api/v1/audit");
        filter.doFilter(auditRequest, new MockHttpServletResponse(), new MockFilterChain());

        verify(repository, never()).save(any(AuditLog.class));
    }
}
