package com.exchange.profile.repository;


import com.exchange.profile.domain.VerifySentEmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VerifySentEmailHistoryRepository extends JpaRepository<VerifySentEmailHistory, String> {

    List<VerifySentEmailHistory> findAllByEmailAndCreateDateBetween(
            String email,
            LocalDateTime start,
            LocalDateTime end
    );


}
