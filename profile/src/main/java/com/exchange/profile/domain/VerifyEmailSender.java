package com.exchange.profile.domain;



public record VerifyEmailSender(String emailTo, String verificationCode, String expiredDate) {
}
