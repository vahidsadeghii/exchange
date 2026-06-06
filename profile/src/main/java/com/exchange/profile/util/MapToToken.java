package com.exchange.profile.util;

import com.exchange.profile.domain.*;

import java.time.LocalDateTime;

public class MapToToken {

    public static TokenResponse mapToTokenResponse(JwtToken jwtToken) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessExpiry = now.plusSeconds(jwtToken.expiresIn());
        LocalDateTime refreshExpiry = now.plusHours(2);

        AccessToken accessToken = new AccessToken(
                jwtToken.accessToken(),
                accessExpiry
        );

        RefreshToken refreshToken = new RefreshToken(
                jwtToken.refreshToken(),
                refreshExpiry
        );
        return new TokenResponse(
                accessToken,
                refreshToken,
                UserType.USER
        );
    }
}
