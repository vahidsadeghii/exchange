package com.exchange.eventmanager.repository;


import com.exchange.eventmanager.domain.EventInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventInfoRepository extends MongoRepository<EventInfo, String> {
}
