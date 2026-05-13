package com.exchange.profile.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    private GenderType genderType;

    private String address;

    private String avatarId;

    private String avatarLink;

    private String fileName;

    private LocalDate birthday;

    private String password;

    private String username;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    private String keycloakUserId;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
