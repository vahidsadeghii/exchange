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
    public TagRouter save(String tag, String titleTopic) {
        return tagRouterRepository.findByTagAndTitleTopic(tag, titleTopic)
                .orElseGet(() -> {
                    var router = tagRouterRepository.save(
                            TagRouter.builder()
                                    .tag(tag)
                                    .titleTopic(titleTopic)
                                    .build()
                    );

                    refreshRout();
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
            log.info("Add new Router and refresh router at this time: " + LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
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
}
