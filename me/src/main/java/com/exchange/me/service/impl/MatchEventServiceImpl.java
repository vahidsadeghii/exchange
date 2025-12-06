package com.exchange.me.service.impl;

import com.exchange.me.domain.MatchEvent;
import com.exchange.me.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchEventServiceImpl implements MatchEventService {
    private final KafkaTemplate<String, String> kafkaTemplateSendMessage;

    @Override
    public MatchEvent saveMatchEvent(MatchEvent matchEvent) {
        kafkaTemplateSendMessage.send(matchEvent.getTopic(), matchEvent.getStatus().name());
        return null;
    }
}
