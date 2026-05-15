package com.exchange.profile.controller.setusernamepassword;

import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.service.UserProfileService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UpgradeUserController {

    private final UserProfileService userProfileService;


    @PostMapping(value = "${api.prefix.secure}/set-password")
    @RolesAllowed({"ROLE_USER"})
    public TokenResponse handle(@RequestBody CreateUserRequest request) {
           return userProfileService.upgradeUser(request.username(),  request.email(), request.password());
    }
}
