package com.exchange.profile.controller.userregister;

import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserRegisterController {
    private final UserProfileService userProfileService;


    @PostMapping("/open/register")
    public TokenResponse register(@RequestBody RegisterRequest request) {
        JwtToken token = userProfileService.registerUserProfile(
                request.username(),
                request.email(),
                request.password()
        );

        return new TokenResponse(token.accessToken(), token.refreshToken(), token.expiresIn());
    }
}
