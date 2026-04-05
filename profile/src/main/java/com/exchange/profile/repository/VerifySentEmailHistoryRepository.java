package com.exchange.profile.repository;


import com.exchange.profile.domain.VerifySentEmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerifySentEmailHistoryRepository extends JpaRepository<VerifySentEmailHistory, String> {

    List<VerifySentEmailHistory> findAllByEmailAndDate(String email, Date date);

    VerifySentEmailHistory findAllByEmail(String email);

    Optional<VerifySentEmailHistory> findFirstByUserIdOrderByCreateDateDesc(String userId);

    Optional<VerifySentEmailHistory> findByVerificationCode(String verificationCode);

}
