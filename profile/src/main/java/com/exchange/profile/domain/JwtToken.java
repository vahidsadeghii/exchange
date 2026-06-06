package com.exchange.profile.domain;

public record JwtToken(
        String accessToken,
        String refreshToken,
        long expiresIn){
}
