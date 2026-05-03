package com.exchange.event.repository;


import com.exchange.event.domain.TagRouter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRouterRepository extends JpaRepository<TagRouter, Long> {
    Optional<TagRouter> findByTag(String tag);
    Optional<TagRouter> findByTagAndTitleTopic(String tag, String titleTopic);
}
