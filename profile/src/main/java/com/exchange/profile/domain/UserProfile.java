package com.exchange.profile.domain;

import com.opencsv.bean.CsvDate;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;


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

    private String keycloakUserId;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
