package com.exchange.wallet.config.security;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Getter
@Setter
@Builder
public class OnlineUser implements UserDetails {
    private final String keycloakUserId;
    private final Long internalUserId;
    private final List<String> roles;

    public OnlineUser(String keycloakUserId,
                      Long internalUserId,
                      List<String> roles) {

        this.keycloakUserId = keycloakUserId;
        this.internalUserId = internalUserId;
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