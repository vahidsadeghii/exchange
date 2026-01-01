package com.exchange.profile.config.security;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JWTAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public @Nullable AbstractAuthenticationToken convert(Jwt source) {
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(source);

        return new JwtAuthenticationToken(source, authorities);
    }


    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt){
        List<String> roles = jwt.getClaim("realm_access") != null
                ? ((Map<String, List<String>>)jwt.getClaim("realm_access")).get("roles")
                :List.of();

        return roles.stream().map(SimpleGrantedAuthority::new).toList();
    }
}
