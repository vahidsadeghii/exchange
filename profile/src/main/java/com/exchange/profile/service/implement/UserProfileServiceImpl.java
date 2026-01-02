package com.exchange.profile.service.implement;

import com.exchange.profile.config.keycloak.KeycloakTokenClient;
import com.exchange.profile.domain.*;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.exception.UserCanNotFoundException;
import com.exchange.profile.repository.UserProfileRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.util.PasswordEncoderUtil;
import jakarta.ws.rs.NotFoundException;
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

        String keycloakUserId = null;

        try {
            Response response = keycloakAdmin.realm(targetRealm)
                    .users()
                    .create(kcUser);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
            }

            keycloakUserId = CreatedResponseUtil.getCreatedId(response);

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(password);
            passwordCred.setTemporary(false);

            keycloakAdmin.realm(targetRealm)
                    .users()
                    .get(keycloakUserId)
                    .resetPassword(passwordCred);

            UserResource userResource = keycloakAdmin.realm(targetRealm).users().get(keycloakUserId);

            ensureRealmRolesExist(targetRealm, Arrays.stream(UserAuthority.values())
                    .map(UserAuthority::name)
                    .toList());

            List<RoleRepresentation> rolesToAssign = new ArrayList<>();
            for (UserAuthority authority : UserAuthority.values()) {
                try {
                    RoleRepresentation role = keycloakAdmin.realm(targetRealm)
                            .roles()
                            .get(authority.name())
                            .toRepresentation();
                    rolesToAssign.add(role);
                } catch (NotFoundException ex) {
                    log.warn("Role {} not found in Keycloak, skipping", authority);
                }
            }
            if (!rolesToAssign.isEmpty()) {
                userResource.roles().realmLevel().add(rolesToAssign);
                log.info("Assigned {} roles to Keycloak user {}", rolesToAssign.size(), keycloakUserId);
            }
            userProfile.setKeycloakUserId(keycloakUserId);
            userProfileRepository.save(userProfile);

            return keycloakTokenClient.getToken(username, password);

        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            if (keycloakUserId != null) {
                try {
                    keycloakAdmin.realm(targetRealm).users().get(keycloakUserId).remove();
                    log.info("Keycloak user {} removed due to failure", keycloakUserId);
                } catch (Exception ex) {
                    log.error("Failed to remove Keycloak user {}", keycloakUserId, ex);
                }
            }
            throw new RuntimeException("Failed to register user", e);
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

    private void ensureRealmRolesExist(String realm, List<String> roles) {
        for (String roleName : roles) {
            try {
                keycloakAdmin.realm(realm).roles().get(roleName).toRepresentation();
            } catch (NotFoundException e) {
                RoleRepresentation newRole = new RoleRepresentation();
                newRole.setName(roleName);
                keycloakAdmin.realm(realm).roles().create(newRole);
                log.info("Created missing Keycloak realm role: {}", roleName);
            }
        }
    }
}
