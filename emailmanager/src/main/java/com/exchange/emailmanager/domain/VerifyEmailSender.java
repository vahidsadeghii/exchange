package com.exchange.emailmanager.domain;


public record VerifyEmailSender(String emailTo, String verifySentEmailHistoryId, String verificationCode, String expiredDate)  {
}
