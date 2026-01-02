package com.exchange.profile.controller.getuser;


import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FinsUserProfileController {
    private final UserProfileService userProfileService;


    @GetMapping(value = "/user")
    @PreAuthorize("hasRole('GET_PROFILE')")
    public String handle(){
        userProfileService.getProfile(13);

        return "find user successfully";
    }
}
