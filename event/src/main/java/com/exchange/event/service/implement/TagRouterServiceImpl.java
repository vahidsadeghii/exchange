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
    private final AsyncRefreshService asyncRefreshService;


    @Override
    public TagRouter save(String tag, String titleTopic) {
        return tagRouterRepository.findByTagAndDestinationTopic(tag, titleTopic)
                .orElseGet(() -> {
                    var router = tagRouterRepository.save(
                            TagRouter.builder()
                                    .tag(tag)
                                    .destinationTopic(titleTopic)
                                    .build()
                    );

                    asyncRefreshService.refreshAsync(this);
                    log.info("Added new topic '{}' to the router", tag);

                    return router;
                });
    }

    @PostConstruct
    public void initRoutes() {
        try {
            camelContext.addRoutes(new EventManagerRouter(camelContext, eventInfoService, this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshRout() {
        try {
            camelContext.getRouteController().stopRoute("sourceKafka");
            camelContext.removeRoute("sourceKafka");

            camelContext.addRoutes(new EventManagerRouter(camelContext, eventInfoService, this));

            log.info("Router refreshed at: " + LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TagRouter> findAll() {
        return tagRouterRepository.findAll();
    }

    @Override
    public List<TagRouter> findByTag(String tag) {
        return List.of();
    }

    @Override
    public TagRouter findByTagAndTitleTopic(String tag, String titleTopic) {
        return tagRouterRepository.findByTagAndDestinationTopic(tag, titleTopic).orElse(null);
    }
}