package com.sivion.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class TenantIsolationFilterTest {
    private final TenantIsolationFilter filter = new TenantIsolationFilter();

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsClientSelectedTenantOtherThanSupportedTenant() throws Exception {
        MockHttpServletRequest request = apiRequest();
        request.addHeader("X-Tenant-ID", "2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsTenantOneForAccountsWithoutTenantClaim() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication(null));
        MockHttpServletRequest request = apiRequest();
        request.addHeader("X-Tenant-ID", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsJwtTenantClaimThatDoesNotMatchCurrentSingleTenantDeployment() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication("2"));
        MockHttpServletRequest request = apiRequest();
        request.addHeader("X-Tenant-ID", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    private MockHttpServletRequest apiRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        return request;
    }

    private JwtAuthenticationToken authentication(String tenantId) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "siva");
        if (tenantId != null) builder.claim("tenant_id", tenantId);
        return new JwtAuthenticationToken(
            builder.build(),
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
