package com.exchange.event.service;


import com.exchange.event.domain.TagRouter;

import java.util.List;

public interface TagRouterService {
    TagRouter save(String tag, String titleTopics);
    List<TagRouter> findAll();
    TagRouter findByTagAndTitleTopic(String tag, String titleTopic);

    List<TagRouter> findAllByTag(String tag);
}
