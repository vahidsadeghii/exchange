package com.exchange.wallet.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;


@Component
public class JWTAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        OnlineUser principal = new OnlineUser(
                jwt.getSubject(),
                jwt.getClaimAsString("userId") != null
                        ? Long.valueOf(jwt.getClaimAsString("userId"))
                        : null,
                extractRoles(jwt)
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt,
                authorities
        );
    }

    private List<String> extractRoles(Jwt jwt) {
        Object realmAccessObj = jwt.getClaim(REALM_ACCESS);

        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }

        Object rolesObj = realmAccess.get(ROLES);

        if (!(rolesObj instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return extractRoles(jwt).stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

}
