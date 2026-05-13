package com.exchange.profile.service;


import com.exchange.profile.domain.VerifySentEmailHistory;

public interface VerifySentEmailHistoryService {
   /**
    * Save  VerifySentEmailHistory and send email to user to accept his/her email
    * @param email throw exception if the user exist else send email verification to user
    * @return VerifySentEmailHistory
    */
   VerifySentEmailHistory registerEmail(String email);


}
