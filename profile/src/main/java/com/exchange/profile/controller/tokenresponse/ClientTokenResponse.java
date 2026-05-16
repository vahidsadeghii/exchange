package com.exchange.profile.controller.tokenresponse;


public record ClientTokenResponse(AccessTokenResponse accessTokenResponse,
                                  RefreshTokenResponse refreshTokenResponse) {
}
