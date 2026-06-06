package com.exchange.profile.domain;



public record TokenResponse(AccessToken accessToken, RefreshToken refreshToken, UserType userType){
}
