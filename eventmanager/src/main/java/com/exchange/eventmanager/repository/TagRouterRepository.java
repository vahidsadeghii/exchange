package com.exchange.eventmanager.repository;

import com.exchange.eventmanager.domain.TagRouter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRouterRepository extends MongoRepository<TagRouter, String> {

    Optional<TagRouter> findByTag(String tag);

    Optional<TagRouter> findByTagAndTitleTopic(String tag, String titleTopic);
}
