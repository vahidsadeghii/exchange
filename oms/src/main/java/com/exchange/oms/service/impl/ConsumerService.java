package com.exchange.oms.service.impl;

import com.exchange.oms.domain.MatchEngineUpdate;
import com.exchange.oms.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
    private final OrderService  orderService;


    @KafkaListener(topics = "${custom-config.kafka.updatematchingEngine-input-message.topic}")
    public void updateMatchEngineEvent(String message) throws IOException{
     ObjectMapper objectMapper = new ObjectMapper();
     try {
         MatchEngineUpdate matchEngineEvent = objectMapper.readValue(message, MatchEngineUpdate.class);
         orderService.updateOrder(matchEngineEvent.orderId(), , matchEngineEvent.status());
     }catch (Exception e){
         throw new IOException(e);
     }
    }
}




