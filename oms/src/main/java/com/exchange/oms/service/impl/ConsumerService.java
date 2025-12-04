package com.exchange.oms.service.impl;

import com.exchange.oms.domain.MatchEngineUpdate;
import com.exchange.oms.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "${custom-config.kafka.updatematchingEngine-input-message.topic}",
            groupId = "oms-consumer-group")
    public void updateMatchEngineEvent(String message) {
        try {
            MatchEngineUpdate matchEngineEvent =
                    objectMapper.readValue(message, MatchEngineUpdate.class);
            orderService.updateOrder(
                    matchEngineEvent.orderId(),
                    matchEngineEvent.userId(),
                    matchEngineEvent.status()
            );

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
            throw new RuntimeException("Kafka message processing failed", e);
        }
    }
}




