package com.exchange.profile.domain;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifySentEmailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String email;
    private String verificationCode;
    private LocalDateTime expiredDate;
    private int tryCount;

    @Enumerated(EnumType.STRING)
    private VerifyEmailStatus status;

    private LocalDateTime createDate;

    private LocalDateTime lastModifiedDate;
}
