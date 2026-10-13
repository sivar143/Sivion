package com.sivion.api.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Captures write-request metadata centrally so individual controllers do not need
 * to duplicate audit logic. Request bodies and query strings are intentionally excluded.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogFilter.class);

    private final AuditLogRepository repository;

    public AuditLogFilter(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (isAuditableWrite(request)) {
                saveAuditRecord(request, response.getStatus());
            }
        }
    }

    private boolean isAuditableWrite(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        boolean isWrite = "POST".equals(method)
                || "PUT".equals(method)
                || "PATCH".equals(method)
                || "DELETE".equals(method);

        // Do not generate audit noise for the audit screen or infrastructure endpoints.
        return isWrite
                && path.startsWith("/api/")
                && !path.startsWith("/api/v1/audit");
    }

    private void saveAuditRecord(HttpServletRequest request, int statusCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? "anonymous"
                : authentication.getName();
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "unspecified";
        }

        AuditLog record = new AuditLog(
                tenantId,
                actor,
                request.getMethod(),
                request.getRequestURI(),
                statusCode,
                Instant.now(),
                request.getRemoteAddr()
        );

        try {
            repository.save(record);
        } catch (RuntimeException exception) {
            // An audit-storage outage must not mask the response from the business endpoint.
            LOGGER.warn("Could not persist audit metadata for {} {}", request.getMethod(),
                    request.getRequestURI(), exception);
        }
    }
}
