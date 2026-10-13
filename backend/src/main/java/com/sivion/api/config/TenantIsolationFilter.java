package com.sivion.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The current data services are pinned to tenant 1. Reject any attempt to
 * select another tenant until the application has tenant-aware repositories
 * and a verified tenant-membership model.
 */
@Component
public class TenantIsolationFilter extends OncePerRequestFilter {
    private static final String SUPPORTED_TENANT = "1";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestedTenant = request.getHeader("X-Tenant-ID");
        if (requestedTenant != null && !SUPPORTED_TENANT.equals(requestedTenant.trim())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The requested tenant is not supported by this deployment");
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Object tenantClaim = jwtAuthentication.getToken().getClaims().get("tenant_id");
            if (tenantClaim == null) {
                tenantClaim = jwtAuthentication.getToken().getClaims().get("tenantId");
            }
            if (tenantClaim != null && !SUPPORTED_TENANT.equals(tenantClaim.toString())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "The authenticated account is not assigned to the supported tenant");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
