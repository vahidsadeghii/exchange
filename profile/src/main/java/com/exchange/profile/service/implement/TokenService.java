package com.exchange.profile.service.implement;

import com.exchange.profile.config.keycloak.KeycloakTokenClient;
import com.exchange.profile.domain.JwtToken;
import com.exchange.profile.exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        String userId = createKeycloakUser(email, internalUserId);

        assignRealmRoles(userId, List.of("ROLE_USER"));

        String tempPassword = UUID.randomUUID().toString();
        setTemporaryPassword(userId, tempPassword);

        return keycloakTokenClient.getToken(email, tempPassword);
    }

    // STEP 2: Set password → ROLE_CUSTOMER → login
    public JwtToken upgradeUser(String userId, String email, String password) {
        removeRealmRoles(userId, List.of("ROLE_USER"));
        assignRealmRoles(userId, List.of("ROLE_CUSTOMER"));

        setUserPassword(userId, password);

        return keycloakTokenClient.getToken(email, password);
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
                "userId", List.of(String.valueOf(internalUserId)),
                "source", List.of("app-registration")
        ));

        var response = realm().users().create(kcUser);

        if (response.getStatus() == 201) {
            return CreatedResponseUtil.getCreatedId(response);
        }

        if (response.getStatus() == 409) {
            List<UserRepresentation> users = realm().users().search(email, true);
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
            throw new UserAlreadyExistException();
        }

        throw new IllegalStateException(
                "Failed to create Keycloak user: " + response.getStatus() + " " + response.getStatusInfo()
        );
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


    // Assign realm roles
    private void assignRealmRoles(String userId, List<String> roles) {
        UserResource user = realm().users().get(userId);
        List<RoleRepresentation> reps = roles.stream()
                .map(r -> realm().roles().get(r).toRepresentation())
                .toList();

        user.roles().realmLevel().add(reps);
    }

    // Remove realm roles
    private void removeRealmRoles(String userId, List<String> roles) {

        UserResource user = realm().users().get(userId);

        List<RoleRepresentation> reps = roles.stream()
                .map(r -> realm().roles().get(r).toRepresentation())
                .toList();

        user.roles().realmLevel().remove(reps);
    }
}