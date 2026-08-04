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
                .log("Message received = ${body}")
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

                        log.info("TAG = {}", eventInfoMessage.getTag());
                        log.info("Routers = {}", tagRouterService.findAll());

                        if (eventInfoMessage.isPersistent()) {
                            eventInfoService.save(eventInfoMessage.getTag(),
                                    eventInfoMessage.getTitle(),
                                    eventInfoMessage.getServiceName(),
                                    objectMapper.writeValueAsString(eventInfoMessage.getEvent()),
                                    LocalDateTime.now());

                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }).choice();
        if (tags.size() > 0) {
            Map<String, List<TagRouter>> topics = tags.stream().collect(Collectors.groupingBy(TagRouter::getTag));
            topics.forEach((k, v) ->
                    choice.when()
                            .jsonpath("$.[?(@.tag == '" + k + "' )]")
                            .transform(ExpressionBuilder.languageExpression("jsonpath", "$.event")).marshal().json(JsonLibrary.Jackson)
                            .multicast()
                            .to(v.stream().map(tagRout -> "kafka:" + tagRout.getTitleTopic() + "?brokers=kafka-service:9092")
                                    .collect(Collectors.toList()).toArray(String[]::new))
                            .log("Send message to topic:  " + v.get(0).getTitleTopic()));
            choice.endChoice();
        }
    }

}//@.routingEnabled == true &&