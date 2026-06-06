package com.exchange.emailmanager.controller.verifyemail;

import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class VerifyEmailRequest {
    private String emailTo;
    private String verifySentEmailHistoryId;
    private String verifyCode;
    private LocalDateTime expirationDate;
}
