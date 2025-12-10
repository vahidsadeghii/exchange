package com.exchange.profile.service;


import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.UserProfile;

import java.time.LocalDate;
import java.util.List;

public interface UserProfileService {

    UserProfile saveUserProfile(String onlineUserId,
                                      String userName, String lastName, String phoneNumber,
                                      String avatarId, String address, GenderType genderType,
                                      LocalDate birthday, String avtarLink, String fileName);

    UserProfile getProfile(String onlineUserId);

    UserProfile findUserById(String userId);

    List<UserProfile> findAllUsers();
}
