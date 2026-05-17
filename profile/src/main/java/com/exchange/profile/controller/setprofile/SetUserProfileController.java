package com.exchange.profile.controller.setprofile;


import com.exchange.profile.config.security.OnlineUser;
import com.exchange.profile.domain.UserProfile;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SetUserProfileController {
    private final UserProfileService userProfileService;


    @PostMapping(value = "${api.prefix.secure}/set-profile")
    @PreAuthorize("hasRole('CUSTOMER')") // IMPORTANT FIX (see below)
    public SetUserProfileResponse setUserProfile(
            @AuthenticationPrincipal OnlineUser onlineUser,
            @RequestBody SetUserProfileRequest request) {
        UserProfile response = userProfileService.setUserProfile(
                onlineUser.getUserId(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.genderType(),
                request.birthday(),
                request.address()
        );

        return new SetUserProfileResponse(
                response.getFirstName(),
                response.getLastName(),
                response.getPhoneNumber(),
                response.getEmail(),
                response.getUserStatus(),
                response.getGenderType(),
                response.getBirthday(),
                response.getAddress()
        );
    }
}
