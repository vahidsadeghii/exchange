package com.exchange.profile.service.implement;

import com.exchange.profile.config.keycloak.KeycloakTokenClient;
import com.exchange.profile.domain.*;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.exception.UserCanNotFoundException;
import com.exchange.profile.repository.UserProfileRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.util.PasswordEncoderUtil;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.data.repository.util.ClassUtils.ifPresent;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
    private final Keycloak keycloakAdmin;
    private final UserProfileRepository userProfileRepository;
    private final KeycloakTokenClient keycloakTokenClient;

    @Value("${keycloak.realm}")
    private String targetRealm;


    @Override
    public JwtToken registerUserProfile(String username, String email, String password) {
        Optional<UserProfile> profile = userProfileRepository.findByEmail(email);
        if(profile.isPresent()){
            throw new UserAlreadyExistException();
        }

        UserProfile userProfile = userProfileRepository.save(UserProfile.builder()
                .email(email)
                .password(PasswordEncoderUtil.encodePassword(password))
                .username(username)
                .build());

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(username);
        kcUser.setEmail(email);
        kcUser.setEnabled(true);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("authorities", List.of(
                UserAuthority.DELETE_PROFILE.name(),
                UserAuthority.GET_PROFILE.name(),
                UserAuthority.UPDATE_PROFILE.name(),
                UserAuthority.SET_PROFILE.name()
                ));
        attributes.put("userId", List.of(userProfile.getId().toString()));
        attributes.put("userType", List.of(UserType.USER.name()));
        attributes.put("source", List.of("app-registration"));
        kcUser.setAttributes(attributes);

        Response response = keycloakAdmin.realm(targetRealm)
                .users()
                .create(kcUser);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        String keycloakUserId = CreatedResponseUtil.getCreatedId(response);

        try {
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            passwordCred.setTemporary(false);

            keycloakAdmin.realm(targetRealm)
                    .users()
                    .get(keycloakUserId)
                    .resetPassword(passwordCred);

            UserResource userResource = keycloakAdmin.realm(targetRealm).users().get(keycloakUserId);
            List<RoleRepresentation> rolesToAssign = new ArrayList<>();
            for (UserAuthority authority : UserAuthority.values()) {
                RoleRepresentation role = keycloakAdmin.realm(targetRealm)
                        .roles()
                        .get(authority.toString())
                        .toRepresentation();
                rolesToAssign.add(role);
            }
            userResource.roles().realmLevel().add(rolesToAssign);
            userProfile.setKeycloakUserId(keycloakUserId);

            return keycloakTokenClient.getToken(username, password);

        } catch (Exception e) {
            keycloakAdmin.realm(targetRealm).users().get(keycloakUserId).remove();
            throw new RuntimeException("Failed to save user locally or assign roles; Keycloak user removed", e);
        }
    }

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
}
