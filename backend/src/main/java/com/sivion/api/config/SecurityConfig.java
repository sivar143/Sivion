package com.sivion.api.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TenantIsolationFilter tenantIsolationFilter
    ) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/v1/supply-chain/track/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakConverter()))
            );

        http.addFilterAfter(tenantIsolationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    FilterRegistrationBean<TenantIsolationFilter> tenantIsolationFilterRegistration(
            TenantIsolationFilter tenantIsolationFilter
    ) {
        FilterRegistrationBean<TenantIsolationFilter> registration =
            new FilterRegistrationBean<>(tenantIsolationFilter);
        // Run after JWT authentication inside Spring Security, not once before it as a servlet filter.
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakConverter() {
        return jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof Map<?, ?> map) {
                Object roles = map.get("roles");
                if (roles instanceof Collection<?> collection) {
                    collection.forEach(role -> authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    ));
                }
            }
            return new JwtAuthenticationToken(
                jwt,
                authorities,
                jwt.getClaimAsString("preferred_username")
            );
        };
    }
}
