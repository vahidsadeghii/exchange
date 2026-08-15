package com.exchange.me.repository;

import com.exchange.me.domain.MatchInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MatchInfoRepository extends MongoRepository<MatchInfo, Long> {
}
