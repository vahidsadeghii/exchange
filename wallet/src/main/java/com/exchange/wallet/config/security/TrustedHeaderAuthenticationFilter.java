package com.exchange.wallet.config.security;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
public class TrustedHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String rolesHeader = request.getHeader("X-Roles");

        if (userId != null) {

            List<GrantedAuthority> authorities =
                    Arrays.stream(Optional.ofNullable(rolesHeader).orElse("")
                                    .split(","))
                            .filter(r -> !r.isBlank())
                            .map(SimpleGrantedAuthority::new)
                            .<GrantedAuthority>map(a -> a)
                            .toList();

            OnlineUser principal = new OnlineUser(
                    username,
                    Long.valueOf(userId),
                    Arrays.asList(Optional.ofNullable(rolesHeader).orElse("").split(","))
            );

            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
