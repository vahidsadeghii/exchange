package com.exchange.profile.domain;

import java.time.LocalDateTime;

public record RefreshToken(String refreshToken, LocalDateTime expirationDate){
}
