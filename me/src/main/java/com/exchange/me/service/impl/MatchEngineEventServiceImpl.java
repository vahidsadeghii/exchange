package com.exchange.me.service.impl;

import com.exchange.me.domain.MatchEngineEvent;
import com.exchange.me.service.MatchEngineEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchEngineEventServiceImpl implements MatchEngineEventService {
    private final KafkaTemplate<String, String> kafkaTemplateSendMessage;

    @Override
    public MatchEngineEvent saveMatchEngineEvent(MatchEngineEvent matchEngineEvent) {
        kafkaTemplateSendMessage.send(matchEngineEvent.getTopic(), matchEngineEvent.getStatus().name());
        return null;
    }
}
