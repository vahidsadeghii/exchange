package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.exception.MatchEventFailedToSaveException;
import com.exchange.me.exception.OrderCanNotBeNullException;
import com.exchange.me.exception.OrderIdCanNotBeNullException;
import com.exchange.me.exception.UserIdCanNotBeNullException;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom-config.kafka.event-output-message.topic}")
    private String eventTopic;

    @Value("${custom-config.kafka.me-create-update-output-message.topic}")
    String createUpdateMeMessage;


    @Override
    public MatchEngine saveMatchEvent(Order order) {
        validateOrder(order);
        MatchEngineUpdate update =
                new MatchEngineUpdate(
                        order.getId(),
                        order.getUserId(),
                        order.getMatchEngineStatus());

        EventInfoMessage<MatchEngineUpdate> message =
                EventInfoMessage.<MatchEngineUpdate>builder()
                        .tag(createUpdateMeMessage)
                        .title("save-update-order")
                        .serviceName("matchingengine")
                        .persistent(true)
                        .routingEnabled(true)
                        .createDate(LocalDateTime.now())
                        .event(update)
                        .build();
        kafkaTemplate.send(eventTopic, message);

        return new MatchEngine(
                order.getId(),
                order.getUserId(),
                order.getMatchEngineStatus());
    }


    private void sendMatchEvent(Order order, EventInfoMessage eventInfoMessage) {
        kafkaTemplate.send(eventTopic, eventInfoMessage)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send match event message for order: {} user: {}. Topic: {}",
                                order.getId(),
                                order.getUserId(),
                                eventTopic,
                                exception);
                    } else {
                        log.debug("Match event message sent successfully for order: {} user: {}. Partition: {} Offset: {}",
                                order.getId(),
                                order.getUserId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new OrderCanNotBeNullException();
        }

        if (order.getId() == 0) {
            throw new OrderIdCanNotBeNullException();
        }

        if (order.getUserId() == 0) {
            throw new UserIdCanNotBeNullException();
        }
    }

}



