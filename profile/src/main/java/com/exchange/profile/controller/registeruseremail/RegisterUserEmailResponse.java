package com.exchange.profile.controller.registeruseremail;

import java.time.LocalDateTime;

public record RegisterUserEmailResponse(String verifySentEmailHistoryId, String verifyCode, LocalDateTime expiredDate){
}
