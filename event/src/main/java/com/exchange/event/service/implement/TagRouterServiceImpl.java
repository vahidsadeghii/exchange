package com.exchange.event.service.implement;


import com.exchange.event.domain.TagRouter;
import com.exchange.event.repository.TagRouterRepository;
import com.exchange.event.service.EventInfoService;
import com.exchange.event.service.TagRouterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
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
            log.info("INIT ROUTES");

            List<TagRouter> tags = tagRouterRepository.findAll();
            log.info("Initial tags = {}", tags);

            camelContext.addRoutes(new EventManagerRouter(camelContext, eventInfoService, this));

        } catch (Exception e) {
            log.error("Init route failed", e);
        }
    }

    public void refreshRout() {
        try {
            log.info("Stopping route...");

            if (camelContext.getRoute("sourceKafka") != null) {
                camelContext.getRouteController().stopRoute("sourceKafka");
            }

            log.info("Removing route...");
            camelContext.removeRoute("sourceKafka");

            log.info("Routes after remove:");
            camelContext.getRoutes()
                    .forEach(r -> log.info("Current route: {}", r.getRouteId()));

            log.info("Adding new route...");
            camelContext.addRoutes(
                    new EventManagerRouter(camelContext, eventInfoService, this)
            );

            log.info("Route definitions after add:");

            log.info("sourceKafka exists = {}",
                    camelContext.getRoute("sourceKafka") != null);

            log.info("Routes after add:");

            camelContext.getRoutes()
                    .forEach(route ->
                            log.info("Route id = {}", route.getRouteId())
                    );

            camelContext.getRouteController().startRoute("sourceKafka");

            log.info("Route restarted successfully at {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Refresh route failed", e);
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
