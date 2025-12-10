package com.exchange.profile.service.implement;

import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.UserProfile;
import com.exchange.profile.service.UserProfileService;

import java.time.LocalDate;
import java.util.List;

public class UserProfileServiceImpl implements UserProfileService {
    @Override
    public UserProfile saveUserProfile(String onlineUserId, String userName, String lastName, String phoneNumber, String avatarId, String address, GenderType genderType, LocalDate birthday, String avtarLink, String fileName) {
        return null;
    }

    @Override
    public UserProfile getProfile(String onlineUserId) {
        return null;
    }

    @Override
    public UserProfile findUserById(String userId) {
        return null;
    }

    @Override
    public List<UserProfile> findAllUsers() {
        return List.of();
    }
}
