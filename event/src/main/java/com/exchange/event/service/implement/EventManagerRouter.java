package com.exchange.event.service.implement;

import com.exchange.event.domain.EventInfoMessage;
import com.exchange.event.domain.TagRouter;
import com.exchange.event.service.EventInfoService;
import com.exchange.event.service.TagRouterService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.ExpressionBuilder;
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

        log.info("******** NEW EventManagerRouter CREATED ********");
        this.camelContext = camelContext;
        this.eventInfoService = eventInfoService;
        this.tagRouterService = tagRouterService;

        log.info("******** NEW EventManagerRouter CREATED ********");
    }

    @Override
    public void configure() {
        createSourceRoute();
    }

    public void createSourceRoute() {
        log.info("Creating source route");

        List<TagRouter> tags = tagRouterService.findAll();
        log.info("========== CREATE ROUTE ==========");
        log.info("Tags size = {}", tags.size());

        for (TagRouter tag : tags) {
            log.info("TAG FROM DB = [{}]", tag.getTag());
        }

        log.info("Tags size = {}", tags.size());
        log.info("Tags = {}", tags);
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
                                    eventInfoMessage.getTitle(),
                                    eventInfoMessage.getServiceName(),
                                    eventInfoMessage.getEvent().toString(), LocalDateTime.now());
                            log.info("Before choice");

                        }
                    } catch (Exception e) {
                        log.error("Error processing message", e);
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .log("BODY = ${body}")
                .choice();
        if (!tags.isEmpty()) {
            Map<String, List<TagRouter>> topics = tags.stream().collect(Collectors.groupingBy(TagRouter::getTag));
            log.info("Topics: {}", topics.keySet());
            topics.forEach((k, v) -> {
                log.info("Checking route tag = [{}]", k);

                choice.when()
                        .jsonpath("$.tag")
                        .log("MATCHED " + k)
//                        .transform(ExpressionBuilder.languageExpression("jsonpath", "$.event"))
//                        .marshal().json(JsonLibrary.Jackson)
//                        .multicast()
//                        .to(v.stream()
//                                .map(tagRout -> "kafka:" + tagRout.getTitleTopic() + "?brokers=kafka-service:9092")
//                                .toArray(String[]::new))
                        .log("Send message to topic: " + v.getFirst().getTitleTopic());
            });

        }
        choice.otherwise()
                .log("No route matched");
        choice.end();
    }

}//@.routingEnabled == true &&

