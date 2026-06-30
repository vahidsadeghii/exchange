package com.exchange.me.service.impl;

import com.exchange.me.domain.EventInfoMessage;
import com.exchange.me.domain.MatchEngine;
import com.exchange.me.domain.MatchEventStatus;
import com.exchange.me.domain.Order;
import com.exchange.me.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchEventServiceImpl implements MatchEventService {
    private final KafkaTemplate<String, Object> kafkaTemplateSendMessage;

    @Value("${custom-config.kafka.event-output-message.topic}")
    private String eventMessage;

    @Override
    public MatchEngine saveMatchEvent(Order order) {

        //eventMessage
        EventInfoMessage eventInfoMessage = EventInfoMessage.builder()
                .tag(eventMessage)
                .title("event-verify-email")
                .serviceName("matchingengine")
                .persistent(true)
                .routingEnabled(false)
                .createDate(LocalDateTime.now())
                .event(order)
                .build();
        kafkaTemplateSendMessage.send("", eventInfoMessage)
                .whenComplete((r, e) -> {
                    if (e != null)
                        log.error("send error", e);
                    else
                        log.info("message sent");
                });

        return new MatchEngine(order.getId(), order.getUserId(), MatchEventStatus.FILLED);
    }

}
