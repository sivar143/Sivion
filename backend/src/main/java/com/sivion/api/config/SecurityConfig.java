package com.sivion.api.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Configuration
@EnableMethodSecurity
public class SecurityConfig  {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception  {
        http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
        .requestMatchers(
        "/actuator/health",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
        )
        .permitAll()
        .anyRequest()
        .authenticated()
        )
        .oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt
        .jwtAuthenticationConverter(
        keycloakConverter()
        )
        )
        );
        return http.build();
    }
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakConverter()  {
        return jwt ->  {
            Set<GrantedAuthority> authorities = new HashSet<>();
            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof Map<?, ?> map)  {
                Object roles = map.get("roles");
                if (roles instanceof Collection<?> collection)  {
                    collection.forEach(role ->
                    authorities.add(
                    new SimpleGrantedAuthority(
                    "ROLE_" + role
                    )
                    )
                    );
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
