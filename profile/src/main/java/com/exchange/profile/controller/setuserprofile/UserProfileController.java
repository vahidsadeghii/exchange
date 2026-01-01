package com.exchange.profile.controller.setuserprofile;


import com.exchange.profile.config.security.OnlineUser;
import com.exchange.profile.domain.GenderType;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    private final UserProfileService userProfileService;
    //private final OnlineUser onlineUser;


    @PostMapping(value="/user")
    public void setUserProfile(@RequestBody UserProfileRequest request){
        //onlineUser.getUserId()
        userProfileService.saveUserProfile(225,
                request.firstName(),request.lastName(), request.phoneNumber(),
               request.avatarId(), request.avatarLink(), GenderType.valueOf(request.genderType()),
               request.birthday(), request.avatarLink(), request.fileName()
                );

    }
}
