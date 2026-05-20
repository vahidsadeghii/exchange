package com.exchange.profile.controller.finduserbykeycloakId;


import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FindUserByKeycloakIdController {
    private final UserProfileService userProfileService;

    @GetMapping(value = "/open/user-id")
    public Optional<Long> handle(@RequestParam String keycloakId) {
           return userProfileService.findUserByKeycloakId(keycloakId);
    }
}
