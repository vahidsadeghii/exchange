package com.exchange.profile.domain;


import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifySentEmailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    private String userId;
    private String email;
    private String verificationCode;
    private LocalDateTime expiredDate;
    private int tryCount;
    private boolean isUsed;

    @Enumerated(EnumType.STRING)
    private VerifyEmailStatus status;

    private LocalDateTime createDate;

    private LocalDateTime lastModifiedDate;
}
