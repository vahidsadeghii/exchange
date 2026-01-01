package com.exchange.profile.domain;

import java.util.List;

public record KeycloakUser(long userId, List<UserAuthority> authorities) {
}
