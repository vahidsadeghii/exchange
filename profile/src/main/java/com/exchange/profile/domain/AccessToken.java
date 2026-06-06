package com.exchange.profile.domain;

import java.time.LocalDateTime;

public record AccessToken(String accessToken, LocalDateTime expirationDate) {
}
