package com.exchange.profile.controller.verifyuseremail;

import java.time.LocalDateTime;

public record VerifyUserEmailResponse (String verifySentEmailHistoryId, String verifyCode, LocalDateTime expiredDate){
}
