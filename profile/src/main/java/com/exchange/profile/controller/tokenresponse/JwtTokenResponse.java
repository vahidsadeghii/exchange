package com.exchange.profile.controller.tokenresponse;


public record JwtTokenResponse(AccessTokenResponse accessTokenResponse,
                               RefreshTokenResponse refreshTokenResponse) {
}
