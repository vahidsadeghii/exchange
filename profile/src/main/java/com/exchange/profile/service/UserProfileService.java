package com.exchange.profile.service;


import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.domain.UserProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserProfileService {

    JwtToken registerUserProfile(String username, String email, String password);

    UserProfile saveUserProfile(long onlineUserId,
                                      String firstName, String lastName, String phoneNumber,
                                      String avatarId, String address, GenderType genderType,
                                      LocalDate birthday, String avtarLink, String fileName);

    UserProfile getProfile(long onlineUserId);

    Optional<UserProfile> findUserById(long userId);

    List<UserProfile> findAllUsers();
}
