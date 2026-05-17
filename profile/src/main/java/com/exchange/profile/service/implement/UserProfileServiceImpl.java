package com.exchange.profile.service.implement;

import com.exchange.profile.domain.*;
import com.exchange.profile.exception.UserCanNotFoundException;
import com.exchange.profile.repository.UserProfileRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.util.MapToToken;
import com.exchange.profile.util.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final Keycloak keycloakAdmin;
    private final UserProfileRepository userProfileRepository;
    private final TokenService tokenService;

    @Value("${keycloak.realm}")
    private String targetRealm;

    @Override
    public UserProfile saveUserProfile(long onlineUserId, String firstName,
                                       String lastName, String phoneNumber,
                                       String avatarId, String address,
                                       GenderType genderType, LocalDate birthday,
                                       String avtarLink, String fileName) {
        Optional<UserProfile> userProfile = findUserById(onlineUserId);
        if (userProfile.isEmpty()) {
            throw new UserCanNotFoundException();
        }
        return userProfileRepository.save(UserProfile.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .avatarId(avatarId)
                .address(address)
                .genderType(genderType)
                .birthday(birthday)
                .avatarLink(avtarLink)
                .fileName(fileName)
                .build());
    }

    @Override
    public UserProfile getProfile(long onlineUserId) {
        return null;
    }

    @Override
    public Optional<UserProfile> findUserById(long userId) {
        return userProfileRepository.findById(userId);
    }

    @Override
    public List<UserProfile> findAllUsers() {
        return List.of();
    }

    @Override
    public Optional<UserProfile> findUserByEmail(String email) {
        return userProfileRepository.findByEmail(email);
    }

    @Override
    public UserProfile createUser(String email) {
        return userProfileRepository.save(UserProfile.builder()
                .email(email)
                .userStatus(UserStatus.ACTIVE)
                .createDate(LocalDateTime.now())
                .build());
    }

    @Override
    public TokenResponse upgradeUser(String username, String email, String password) {
        Optional<UserProfile> userProfile = userProfileRepository.findByEmail(email);
        if (!userProfile.isPresent() || userProfile.get().getUserStatus().equals(UserStatus.INACTIVE)) {
            throw new UserCanNotFoundException();
        }
        String encodePassword = PasswordEncoderUtil.encodePassword(password);
        userProfile.get().setPassword(encodePassword);
        userProfile.get().setUsername(username);
        JwtToken jwtToken = tokenService.upgradeUser(userProfile.get().getId(), email, encodePassword);

        return MapToToken.mapToTokenResponse(jwtToken);
    }

}
