package com.exchange.event.service.implement;

import com.exchange.event.domain.TagRouter;
import com.exchange.event.repository.TagRouterRepository;
import com.exchange.event.service.EventInfoService;
import com.exchange.event.service.TagRouterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TagRouterServiceImpl implements TagRouterService {
    private final TagRouterRepository tagRouterRepository;

    private final CamelContext camelContext;
    private final EventInfoService eventInfoService;
    @Override
    public TagRouter save(String tag, String titleTopics) {

        TagRouter tagRouter = findByTagAndTitleTopic(tag, titleTopics);
        if (tagRouter == null) {
            tagRouter = tagRouterRepository.save(TagRouter.builder()
                    .tag(tag)
                    .titleTopic(titleTopics)
                    .build());
            refreshRout();
            log.info("Add new topic to the router");
        }
        return tagRouter;
    }

    @PostConstruct
    public void initRoutes(){
        try {
            camelContext.addRoutes(new EventManagerRouter(camelContext, eventInfoService, this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


public void refreshRout() {
    try {
        var routeController = camelContext.getRouteController();

        if (camelContext.getRoute("sourceKafka") != null) {
            routeController.stopRoute("sourceKafka");
            camelContext.removeRoute("sourceKafka");
        }

        camelContext.addRoutes(
                new EventManagerRouter(camelContext, eventInfoService, this)
        );

        log.info("Added new router and refreshed at: {}", LocalDateTime.now());

    } catch (Exception e) {
        log.error("Error while refreshing route", e);
    }
}


    @Override
    public List<TagRouter> findAll() {
        return tagRouterRepository.findAll();
    }

    @Override
    public TagRouter findByTagAndTitleTopic(String tag, String titleTopic) {
        return tagRouterRepository.findByTagAndTitleTopic(tag, titleTopic).orElse(null);
    }

    @Override
    public List<TagRouter> findAllByTag(String tag) {
        return tagRouterRepository.findAllByTag(tag.trim().toLowerCase());
    }
}
