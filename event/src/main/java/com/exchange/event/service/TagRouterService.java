package com.exchange.event.service;

import com.exchange.event.domain.TagRouter;

import java.util.List;

public interface TagRouterService {
    TagRouter save(String tag, String titleTopic);

    List<TagRouter> findAll();


    List<TagRouter> findByTag(String tag);

    //void refreshRout();

    TagRouter findByTagAndTitleTopic(String tag, String titleTopic);
}
