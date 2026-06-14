package com.exchange.eventmanager.service;

import com.exchange.eventmanager.domain.TagRouter;

import java.util.List;

public interface TagRouterService {
    TagRouter save(String tag, String titleTopics);

    List<TagRouter> findAll();

    TagRouter findByTagAndTitleTopic(String tag, String titleTopic);
}
