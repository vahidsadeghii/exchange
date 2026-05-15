package com.exchange.profile.service;


import com.exchange.profile.domain.TokenResponse;
import com.exchange.profile.domain.VerifySentEmailHistory;

import java.time.LocalDateTime;

public interface VerifySentEmailHistoryService {
   /**
    * Save  VerifySentEmailHistory and send email to user to accept his/her email
    * @param email throw exception if the user exist else send email verification to user
    * @return VerifySentEmailHistory
    */
   VerifySentEmailHistory registerEmail(String email);

   /**
    *
    * @param verifyCodeId VerifySentEmailHistory-Id
    * @param verifyCode email verification code
    * @param expiredDate verify code expired date
    * @return TokenResponse
    */
   TokenResponse verifyEmailCode(String verifyCodeId, String verifyCode, LocalDateTime expiredDate);


}
