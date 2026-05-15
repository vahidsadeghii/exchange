package com.exchange.profile.service.implement;

import com.exchange.profile.config.keycloak.KeycloakTokenClient;
import com.exchange.profile.domain.*;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.exception.UserCanNotFoundException;
import com.exchange.profile.exception.UserRegistrationException;
import com.exchange.profile.repository.UserProfileRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.util.MapToToken;
import com.exchange.profile.util.PasswordEncoderUtil;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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
    private final KeycloakTokenClient keycloakTokenClient;
    private final TokenService tokenService;

    @Value("${keycloak.realm}")
    private String targetRealm;

    @Override
    public JwtToken jwToken(String username, String email, String password) {


        if (userProfileRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistException();
        }

        UserProfile userProfile = userProfileRepository.save(
                UserProfile.builder()
                        .email(email)
                        .password(PasswordEncoderUtil.encodePassword(password))
                        .username(username)
                        .userStatus(UserStatus.ACTIVE)
                        .createDate(LocalDateTime.now())
                        .build()
        );

        String keycloakUserId = null;

        try {
            ensureAuthenticated();

            // 1. Create user in Keycloak
            keycloakUserId = createKeycloakUser(username, email, password, userProfile.getId());

            // 2. Assign ROLE_USER
            assignRealmRoles(keycloakUserId, List.of("ROLE_CUSTOMER"));

            // 3. Save Keycloak ID
            userProfile.setKeycloakUserId(keycloakUserId);
            userProfileRepository.save(userProfile);

            // 4. Login & return token
            return generateToken(username, password);

        } catch (Exception e) {

            log.error("Registration failed for email={}", email, e);

            rollbackKeycloakUser(keycloakUserId);

            throw new UserRegistrationException();
        }
    }


    private void assignRealmRoles(String userId, List<String> roles) {

        UserResource userResource = keycloakAdmin.realm(targetRealm)
                .users()
                .get(userId);

        List<RoleRepresentation> roleRepresentations = roles.stream()
                .map(role -> keycloakAdmin.realm(targetRealm)
                        .roles()
                        .get(role)
                        .toRepresentation())
                .toList();

        userResource.roles().realmLevel().add(roleRepresentations);

        log.info("Assigned roles {} to user {}", roles, userId);
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

    @Override
    public Optional<UserProfile> findUserByEmail(String email) {
        return userProfileRepository.findByEmail(email);
    }

    @Override
    public JwtToken userEmailVerificationToken(String email) {
        findUserByEmail(email).ifPresent(userProfile -> {
            log.error("A user with the provided details already exists with this email:  {}", email);
            throw new UserAlreadyExistException();
        });


        return null;
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
        JwtToken jwtToken = tokenService.upgradeUser(userProfile.get().getId().toString(), email, encodePassword);

        return MapToToken.mapToTokenResponse(jwtToken);
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

    private void ensureAuthenticated() {
        keycloakAdmin.tokenManager().getAccessToken();
    }

    private RealmResource realm() {
        ensureAuthenticated();
        return keycloakAdmin.realm(targetRealm);
    }

    private String createKeycloakUser(String username, String email, String password, long userId) {

        var kcUser = new UserRepresentation();
        kcUser.setUsername(username);
        kcUser.setEmail(email);
        kcUser.setEnabled(true);

        kcUser.setAttributes(Map.of(
                "userId", List.of(Long.toString(userId)),
                "source", List.of("app-registration")
        ));

        var response = realm().users().create(kcUser);

        if (response.getStatus() != 201) {
            throw new IllegalStateException(
                    "Failed to create Keycloak user: " + response.getStatusInfo()
            );
        }

        var keycloakUserId = CreatedResponseUtil.getCreatedId(response);

        setUserPassword(keycloakUserId, password);

        return keycloakUserId;
    }

    private void setUserPassword(String userId, String password) {

        var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        realm().users()
                .get(userId)
                .resetPassword(credential);
    }


    private JwtToken generateToken(String username, String password) {
        return keycloakTokenClient.getToken(username, password);
    }

    private void rollbackKeycloakUser(String userId) {
        if (userId == null) return;

        try {
            realm().users().get(userId).remove();
        } catch (Exception ex) {
            log.error("Failed to rollback Keycloak user {}", userId, ex);
        }
    }

}
