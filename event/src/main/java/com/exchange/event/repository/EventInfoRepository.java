package com.exchange.event.repository;

import com.exchange.event.domain.EventInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventInfoRepository extends MongoRepository<EventInfo, String> {
}
