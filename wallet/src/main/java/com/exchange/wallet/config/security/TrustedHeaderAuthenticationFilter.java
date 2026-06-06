package com.exchange.wallet.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


@Component
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String tokenInfoHeader = request.getHeader("TokenInfo");
        if (tokenInfoHeader != null) {
            TokenInfo tokenInfo =
                    objectMapper.readValue(tokenInfoHeader, TokenInfo.class);
            List<String> cleanRoles = tokenInfo.roles().stream()
                    .filter(Objects::nonNull)
                    .filter(r -> !r.isBlank())
                    .filter(r -> r.startsWith("ROLE_"))
                    .toList();

            List<GrantedAuthority> authorities = cleanRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();

            OnlineUser principal = new OnlineUser(
                    tokenInfo.keycloakUserId(),
                    cleanRoles);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}