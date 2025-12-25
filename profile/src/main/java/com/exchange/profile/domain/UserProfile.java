package com.exchange.profile.domain;



import com.opencsv.bean.CsvDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @NotNull
    private String lastName;
    private String phoneNumber;
    private String email;
    private GenderType genderType;

    @NotNull
    private String address;
    private String avatarId;
    private String avatarLink;
    private String fileName;

    @NotNull
    @CsvDate
    private LocalDate birthday;

    //@CreatedDate
    @CsvDate
    private LocalDateTime createDate;
    //@LastModifiedDate
    @CsvDate
    private LocalDateTime updateDate;
}
