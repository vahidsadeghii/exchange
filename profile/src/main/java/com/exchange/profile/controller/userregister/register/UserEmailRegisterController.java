package com.exchange.profile.controller.userregister.register;


import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserEmailRegisterController {
    private UserProfileService userProfileService;
}
