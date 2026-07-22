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
    private final KafkaTemplate<String, Object> kafkaTemplateSendMessage;

    @Value("${custom-config.kafka.event-output-message.topic}")
    private String eventMessage;

    @Value("${custom-config.kafka.me-create-update-output-message.topic}")
    String createUpdateMeMessage;


    @Override
    public MatchEngine saveMatchEvent(Order order) {
        validateOrder(order);

        MatchEngineUpdate matchEngineUpdate = new MatchEngineUpdate(order.getId(), order.getUserId(), order.getMatchEngineStatus());

        try {
            sendMatchEvent(order, EventInfoMessage.builder()
                    .tag(createUpdateMeMessage)
                    .title("save-update-order")
                    .serviceName("matchingengine")
                    .persistent(true)
                    .routingEnabled(false)
                    .createDate(LocalDateTime.now())
                    .event(matchEngineUpdate)
                    .build());

            log.info("Match event saved successfully for order: {} user: {}", order.getId(), order.getUserId());
            return new MatchEngine(order.getId(), order.getUserId(), MatchEventStatus.FILLED);

        } catch (Exception e) {
            log.error("Unexpected error while saving match event for order: {} user: {}", order.getId(), order.getUserId(), e);

            throw new MatchEventFailedToSaveException();
        }
    }


    private void sendMatchEvent(Order order, EventInfoMessage eventInfoMessage) {
        kafkaTemplateSendMessage.send(eventMessage, eventInfoMessage)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send match event message for order: {} user: {}. Topic: {}",
                                order.getId(),
                                order.getUserId(),
                                eventMessage,
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



