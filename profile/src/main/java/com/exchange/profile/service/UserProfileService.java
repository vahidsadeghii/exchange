package com.exchange.profile.service;


import com.exchange.profile.domain.GenderType;
import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.domain.UserProfile;
import org.apache.kafka.common.quota.ClientQuotaAlteration;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user profiles and authentication-related operations.
 */
public interface UserProfileService {

    /**
     * Creates or updates a user profile for an online user.
     *
     * @param onlineUserId the ID of the online user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param phoneNumber the user's phone number
     * @param avatarId the identifier of the user's avatar
     * @param address the user's address
     * @param genderType the gender type of the user
     * @param birthday the user's date of birth
     * @param avtarLink the URL or link to the user's avatar image
     * @param fileName the avatar file name
     * @return the saved {@link UserProfile}
     */
    UserProfile saveUserProfile(long onlineUserId,
                                String firstName,
                                String lastName,
                                String phoneNumber,
                                String avatarId,
                                String address,
                                GenderType genderType,
                                LocalDate birthday,
                                String avtarLink,
                                String fileName);

    /**
     * Retrieves the user profile for the given online user ID.
     *
     * @param onlineUserId the online user ID
     * @return the {@link UserProfile} if found
     */
    UserProfile getProfile(long onlineUserId);

    /**
     * Finds a user by their internal user ID.
     *
     * @param userId the user ID
     * @return an {@link Optional} containing the {@link UserProfile} if found
     */
    Optional<UserProfile> findUserById(long userId);

    /**
     * Retrieves all registered user profiles.
     *
     * @return a list of all {@link UserProfile} entries
     */
    List<UserProfile> findAllUsers();

    /**
     * Finds a user profile by email address.
     *
     * @param email the user's email
     * @return an {@link Optional} containing the {@link UserProfile} if found
     */
    Optional<UserProfile> findUserByEmail(String email);

    /**
     * Creates a new user account using the provided email.
     *
     * @param email the email of the new user
     * @return the created {@link UserProfile}
     */
    UserProfile createUser(String email);

    /**
     * Upgrades a user account (e.g., registration completion or role upgrade)
     * and returns authentication tokens.
     *
     * @param username the username
     * @param email the user's email
     * @param password the user's password
     * @return a {@link TokenResponse} containing access and refresh tokens
     */
    TokenResponse upgradeUser(String username, String email, String password);

    /**
     * Updates the user's profile information.
     *
     * @param onlineUser the online user identifier
     * @param firstName the first name
     * @param lastName the last name
     * @param phoneNumber the phone number
     * @param genderType the gender type
     * @param birthday the date of birth
     * @param address the address
     * @return the updated {@link UserProfile}
     */
    UserProfile setUserProfile(String onlineUser,
                               String firstName,
                               String lastName,
                               String phoneNumber,
                               GenderType genderType,
                               LocalDate birthday,
                               String address);

    /**
     * Authenticates a user and generates access and refresh tokens.
     * Also records login history.
     *
     * @param userName the username (email)
     * @param password the user's password
     * @return a {@link TokenResponse} containing access and refresh tokens
     */
    TokenResponse signInUser(String userName, String password);

    /**
     * Finds a user by their Keycloak ID.
     *
     * @param keycloakId the Keycloak identifier
     * @return an {@link Optional} containing the user ID if found
     */
    Optional<Long> findUserByKeycloakId(String keycloakId);


    void deleteUserByEmail(String email);
}
