package com.exchange.emailmanager.domain;


public record VerifyEmailSender(String emailTo, String verificationCode, String expiredDate) {
}
