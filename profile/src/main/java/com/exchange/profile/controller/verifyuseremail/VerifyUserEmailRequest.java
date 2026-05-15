package com.exchange.profile.controller.verifyuseremail;

import java.time.LocalDateTime;

public record VerifyUserEmailRequest(String verifySentEmailHistoryId, String verifyCode, LocalDateTime expiredDate) {
}
