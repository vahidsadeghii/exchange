package com.exchange.wallet.domain;

public record JwtToken(
        String accessToken,
        String refreshToken,
        long expiresIn){
}
