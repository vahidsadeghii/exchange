package com.exchange.profile.service.implement;

import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.UserProfile;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.repository.UserProfileRepository;
import com.exchange.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepository userProfileRepository;


    @Override
    public UserProfile saveUserProfile(long onlineUserId, String userName, String lastName, String phoneNumber, String avatarId,
                                       String address, GenderType genderType, LocalDate birthday, String avtarLink, String fileName) {
        UserProfile userProfile = userProfileRepository.findByUserId(onlineUserId).orElseThrow(UserAlreadyExistException::new);

        // TODO: The user details should be retrieved from the authentication app
        return userProfileRepository.save(UserProfile.builder()
                .userId(onlineUserId)
                .firstName(userName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .email(null)
                .build());
    }

    @Override
    public UserProfile getProfile(long onlineUserId) {
        return null;
    }

    @Override
    public UserProfile findUserById(long userId) {
        return null;
    }

    @Override
    public List<UserProfile> findAllUsers() {
        return List.of();
    }
}
