package com.exchange.profile.controller.userregister.register;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
