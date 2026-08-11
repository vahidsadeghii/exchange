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


  @KafkaListener(topics = "${custom-config.kafka.me-create-update-input-message.topic}", groupId = "oms-group")
    public void saveUpdateMatchEngineEvent(String message) {

        try {

            log.info("MATCH ENGINE EVENT RECEIVED: {}", message);

            MatchEngineUpdate matchEngineEvent =
                    objectMapper.readValue(
                            message,
                            MatchEngineUpdate.class
                    );

            log.info(
                    "Processing MatchEngineUpdate: orderId={}, userId={}, status={}",
                    matchEngineEvent.orderId(),
                    matchEngineEvent.userId(),
                    matchEngineEvent.status()
            );

            orderService.matchEngineStatus(
                    matchEngineEvent.orderId(),
                    matchEngineEvent.userId(),
                    matchEngineEvent.status()
            );

            log.info("MatchEngineUpdate processed successfully: orderId={}", matchEngineEvent.orderId());

        } catch (Exception e) {
            log.error("Failed to process MatchEngineUpdate Kafka message: {}", message, e);

            throw new RuntimeException("Kafka MatchEngineUpdate processing failed", e);
        }
    }
}




