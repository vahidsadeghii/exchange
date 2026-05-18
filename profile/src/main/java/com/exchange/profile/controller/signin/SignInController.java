package com.exchange.profile.controller.signin;


import com.exchange.profile.controller.tokenresponse.AccessTokenResponse;
import com.exchange.profile.controller.tokenresponse.RefreshTokenResponse;
import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignInController {
    private final UserProfileService userProfileService;

    @PostMapping(value = "${api.prefix.open}/user/signin")
    public SignInTokenResponse handle(@RequestBody SignInRequest request) {
        TokenResponse tokenResponse = userProfileService.signInUser(request.userName(), request.password());

        return new SignInTokenResponse(
                new AccessTokenResponse(
                        tokenResponse.accessToken().accessToken(),
                        tokenResponse.accessToken().expirationDate()),
                new RefreshTokenResponse(
                        tokenResponse.refreshToken().refreshToken(),
                        tokenResponse.refreshToken().expirationDate()
                ), tokenResponse.userType());
    }
}
