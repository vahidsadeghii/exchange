package com.exchange.profile.config.security;

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

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        Long internalUserId =
                Long.valueOf(jwt.getClaimAsString("internalUserId"));

        OnlineUser principal = new OnlineUser(
                jwt.getSubject(),
                internalUserId,
                extractRoles(jwt));

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt,
                authorities
        );
    }

    private List<String> extractRoles(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null) return List.of();

        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof List<?> roles)) return List.of();

        return roles.stream()
                .map(String::valueOf)
                .filter(r -> r.startsWith("ROLE_"))
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
