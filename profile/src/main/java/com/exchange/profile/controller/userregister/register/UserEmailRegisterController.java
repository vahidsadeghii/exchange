package com.exchange.profile.controller.userregister.register;


import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserEmailRegisterController {
    private UserProfileService userProfileService;



    @PostMapping(value = "/open/register")
    public TokenResponse handle(@RequestBody RegisterRequest request) {
        JwtToken user = userProfileService.createUser(request.username(), request.email(), request.password());
        return new TokenResponse(user.accessToken(), user.refreshToken(), user.expiresIn());
    }
}
