package com.exchange.event.repository;

import com.exchange.event.domain.TagRouter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRouterRepository extends MongoRepository<TagRouter, String> {

    Optional<TagRouter> findByTagAndDestinationTopic(
            String tag,
            String titleTopic
    );

    List<TagRouter> findByTagAndEnabledTrue(String tag);
}
