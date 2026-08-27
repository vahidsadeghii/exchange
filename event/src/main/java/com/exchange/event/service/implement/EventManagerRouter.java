package com.exchange.event.service.implement;

import com.exchange.event.domain.EventInfoMessage;
import com.exchange.event.domain.TagRouter;
import com.exchange.event.service.EventInfoService;
import com.exchange.event.service.TagRouterService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class EventManagerRouter extends RouteBuilder {

    private final CamelContext camelContext;
    private final EventInfoService eventInfoService;
    @Lazy
    private final TagRouterService tagRouterService;

    public EventManagerRouter(CamelContext camelContext, EventInfoService eventInfoService, TagRouterService tagRouterService) {
        this.camelContext = camelContext;
        this.eventInfoService = eventInfoService;
        this.tagRouterService = tagRouterService;
    }

    @Override
    public void configure() {
        createSourceRoute();
    }

    public void createSourceRoute() {
        List<TagRouter> tags = tagRouterService.findAll();
        ChoiceDefinition choice = from("kafka:event-topic?brokers=kafka-service:9092&groupId=event")
                .id("sourceKafka")
                .process(p -> {

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    objectMapper.configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    try {
                        EventInfoMessage
                                eventInfoMessage = objectMapper.readValue(
                                p.getIn().getBody(String.class),
                                EventInfoMessage.class);
                        if (eventInfoMessage.isPersistent()) {
                            eventInfoService.save(eventInfoMessage.getTag(),
                                    eventInfoMessage.getDestinationTopic(),
                                    eventInfoMessage.getServiceName(),
                                    eventInfoMessage.getEvent().toString(), LocalDateTime.now());

                        }

                        if (eventInfoMessage.isRoutingEnabled()) {
                            log.info("Saving TagRouter: tag={}, topic={}",
                                    eventInfoMessage.getTag(),
                                    eventInfoMessage.getDestinationTopic());
                            tagRouterService.save(eventInfoMessage.getTag(), eventInfoMessage.getDestinationTopic());
                        }


                    } catch (Exception e) {
                        log.error("ERROR PROCESSING EVENT", e);
                        throw new RuntimeException(e.getMessage());
                    }
                }).choice();

        if (!tags.isEmpty()) {
            Map<String, List<TagRouter>> topics = tags.stream()
                    .collect(Collectors.groupingBy(TagRouter::getTag));

            topics.forEach((tag, routers) -> {

                String[] destinations = routers.stream()
                        .map(r -> "kafka:" + r.getDestinationTopic() + "?brokers=kafka-service:9092")
                        .toArray(String[]::new);

                if (destinations.length == 0) {
                    return;
                }

                String topicList = routers.stream()
                        .filter(r -> r.getDestinationTopic() != null && !r.getDestinationTopic().isBlank())
                        .map(TagRouter::getDestinationTopic)
                        .collect(Collectors.joining(","));

                choice.when()
                        .jsonpath("$[?(@.tag == '" + tag + "')]")
                        .log("Matched tag: " + tag)
                        .transform().jsonpath("$.event")
                        .marshal().json(JsonLibrary.Jackson)
                        .multicast().to(destinations)
                        .log("Send message to topics: " + topicList);
            });
            choice.endChoice();

        }

    }

}