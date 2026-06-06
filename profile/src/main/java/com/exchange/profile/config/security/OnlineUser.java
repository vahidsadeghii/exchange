package com.exchange.profile.config.security;


import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Getter
@Setter
@Builder
public class OnlineUser implements UserDetails {
    private final String keycloakUserId; // JWT sub
    // private final Long userId;           // DB ID
    private final List<String> roles;

    public OnlineUser(String keycloakUserId, List<String> roles) {
        this.keycloakUserId = keycloakUserId;
        this.roles = roles;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    @Override
    public String getUsername() {
        return keycloakUserId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}