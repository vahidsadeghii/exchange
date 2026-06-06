package com.exchange.emailmanager.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyRegisterSender {
    private String emailTo;
    private String verificationCode;
    private String link;
    private LocalDateTime expiredDate;
}
