package com.exchange.profile.controller.verifyuseremail;


import com.exchange.profile.controller.tokenresponse.AccessTokenResponse;
import com.exchange.profile.controller.tokenresponse.RefreshTokenResponse;

public record VerifyUserEmailResponse(AccessTokenResponse accessTokenResponse,
                                      RefreshTokenResponse refreshTokenResponse) {
}
