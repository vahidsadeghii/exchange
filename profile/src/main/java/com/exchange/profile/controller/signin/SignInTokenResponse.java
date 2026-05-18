package com.exchange.profile.controller.signin;



import com.exchange.profile.controller.tokenresponse.AccessTokenResponse;
import com.exchange.profile.controller.tokenresponse.RefreshTokenResponse;
import com.exchange.profile.domain.UserType;

public record SignInTokenResponse(AccessTokenResponse accessToken,
                                  RefreshTokenResponse refreshToken,
                                  UserType userType){
}
