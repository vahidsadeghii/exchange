package com.exchange.profile.controller.setusernamepassword;

import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.service.implement.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UpgradeUserController {

    private final UserProfileService userProfileService;
    private final TokenService tokenService;


    @PostMapping(value = "${api.prefix.secure}/set-password")
    @PreAuthorize("hasRole('ROLE_USER')")
    public TokenResponse handle(@RequestBody CreateUserRequest request) {
        TokenResponse tokenResponse = userProfileService.upgradeUser(
                    request.username(),
                    request.email(),
                    request.password());
        return tokenResponse;
    }
}
