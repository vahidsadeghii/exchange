package com.exchange.profile.service.implement;

import com.exchange.profile.config.exception.NotFoundException;
import com.exchange.profile.config.keycloak.KeycloakTokenClient;
import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.ws.rs.core.Response;
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

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final Keycloak keycloakAdmin;
    private final KeycloakTokenClient keycloakTokenClient;

    @Value("${keycloak.realm}")
    private String targetRealm;

    private RealmResource realm() {
        return keycloakAdmin.realm(targetRealm);
    }


    // STEP 1: Verify email → create user → ROLE_USER → temp login
    public JwtToken createUser(String email, long internalUserId) {
        String keycloakUserId = createKeycloakUser(email, internalUserId);
        assignRealmRoles(keycloakUserId, List.of("ROLE_USER"));

        String tempPassword = UUID.randomUUID().toString();
        setTemporaryPassword(keycloakUserId, tempPassword);

        return keycloakTokenClient.getToken(email, tempPassword);
    }

    // STEP 2: Set password → ROLE_CUSTOMER → login
    public JwtToken upgradeUser(long internalUserId, String email, String password) {
        String keycloakUserId = findKeycloakUserIdByInternalId(internalUserId);

        removeRealmRoles(keycloakUserId, List.of("ROLE_USER"));
        assignRealmRoles(keycloakUserId, List.of("ROLE_CUSTOMER"));

        setUserPassword(keycloakUserId, password);

        return keycloakTokenClient.getToken(email, password);
    }

    public String getKeycloakUserId(JwtToken token) {
        return Optional.ofNullable(parseClaims(token.accessToken()).getSubject())
                .filter(sub -> !sub.isBlank())
                .orElseThrow(() -> new IllegalStateException("Missing 'sub' in token"));
    }

    private JWTClaimsSet parseClaims(String accessToken) {
        try {
            return SignedJWT.parse(accessToken).getJWTClaimsSet();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT access token", e);
        }
    }



    public JwtToken refreshAccessToken(String refreshToken) {
        return keycloakTokenClient.refreshToken(refreshToken);
    }


    // Create Keycloak user (email = username)
    private String createKeycloakUser(String email, long internalUserId) {
        List<UserRepresentation> existingUsers = realm().users().search(email, true);

        if (!existingUsers.isEmpty()) {
            throw new UserAlreadyExistException();
        }

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(email);
        kcUser.setEmail(email);
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);

        kcUser.setAttributes(Map.of(
                "internalUserId", List.of(String.valueOf(internalUserId)),
                "source", List.of("app-registration")
        ));
        Response response = realm().users().create(kcUser);
        if (response.getStatus() == 201) {
            return CreatedResponseUtil.getCreatedId(response); // ✅ UUID
        }

        if (response.getStatus() == 409) {
            return realm().users().search(email, true).get(0).getId();
        }

        throw new IllegalStateException("Failed to create Keycloak user");
    }

    // Permanent password
    private void setUserPassword(String userId, String password) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);

        realm().users()
                .get(userId)
                .resetPassword(cred);
    }

    // Temporary password
    private void setTemporaryPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        realm().users().get(userId).resetPassword(credential);
    }

    private void assignRealmRoles(String userId, List<String> roles) {
        if (roles == null || roles.isEmpty()) return;
        UserResource user = realm().users().get(userId);

        List<RoleRepresentation> reps = roles.stream()
                .map(this::safeGetRealmRole)
                .filter(Objects::nonNull)
                .toList();

        if (!reps.isEmpty()) {
            user.roles().realmLevel().add(reps);
        }
    }

    // Remove realm roles
    public void removeRealmRoles(String userId, List<String> roles) {
        if (userId == null || userId.isBlank() || roles == null || roles.isEmpty()) {
            return;
        }

        UserResource user;

        try {
            user = realm().users().get(userId);
            user.toRepresentation(); // validate existence
        } catch (Exception e) {
            log.error("User {} not found in realm {}", userId, targetRealm);
            return;
        }

        List<RoleRepresentation> toRemove = roles.stream()
                .map(this::safeGetRealmRole)
                .filter(Objects::nonNull)
                .toList();

        if (toRemove.isEmpty()) {
            return;
        }

        try {
            //NO listEffective() → avoids your 404 issue
            user.roles().realmLevel().remove(toRemove);

        } catch (Exception e) {
            log.error("Failed removing roles for user {}", userId, e);
        }
    }

    private RoleRepresentation safeGetRealmRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }

        String cleaned = roleName.trim();

        try {
            return realm().roles().get(cleaned).toRepresentation();
        } catch (NotFoundException ignored) {
        }

        try {
            return realm().clients().findAll().stream()
                    .map(c -> realm().clients().get(c.getId()))
                    .map(client -> {
                        try {
                            return client.roles().get(cleaned).toRepresentation();
                        } catch (NotFoundException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    private String findKeycloakUserIdByInternalId(long internalUserId) {
        List<UserRepresentation> users = realm().users()
                .searchByAttributes("internalUserId:" + internalUserId);
        if (users.isEmpty()) {
            throw new RuntimeException("User not found in Keycloak for internalId=" + internalUserId);
        }

        //THIS is the real Keycloak ID
        return users.get(0).getId();
    }

}