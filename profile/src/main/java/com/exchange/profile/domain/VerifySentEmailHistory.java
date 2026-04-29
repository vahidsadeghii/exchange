package com.exchange.profile.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifySentEmailHistory {

    @Id
    @GeneratedValue
    private String id;
    private String userId;
    private String email;
    private String verificationCode;
    private LocalDateTime expiredDate;
    private int tryCount;
    private VerifyEmailStatus status;
    @CreatedDate
    @CreatedBy
    private LocalDateTime createDate;
    @LastModifiedDate
    @LastModifiedBy
    private LocalDateTime lastModifiedDate;
}
