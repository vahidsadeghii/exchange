package com.exchange.profile.controller.userregister;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
