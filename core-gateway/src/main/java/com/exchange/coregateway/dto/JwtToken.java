package com.exchange.coregateway.dto;

public record JwtToken(
        String accessToken,
        String refreshToken,
        long expiresIn){
}
