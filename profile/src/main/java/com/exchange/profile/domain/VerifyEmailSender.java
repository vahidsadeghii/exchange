package com.exchange.profile.domain;



public record VerifyEmailSender(String emailTo, String verifySentEmailHistoryId, String verificationCode, String expiredDate) {
}
