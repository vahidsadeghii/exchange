package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchEventServiceImpl implements MatchEventService {
    private final KafkaTemplate<String, String> kafkaTemplateSendMessage;

    private final OrderBookService orderBookService;

    @Value("${custom-config.kafka.savematchingEngine-output-message.topic}")
    private String savematchingEngineTopic;


    @Override
    public void processOrder(long orderId, long userId, TradePair tradePair, OrderType orderType,
                             boolean isBuyOrder, double quantity,
                             double price) {
        OrderBookHandler book = orderBookService.getOrCreateBook(tradePair);

        Order order = Order.builder()
                .id(orderId)
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .orderSide(isBuyOrder ? TradeSide.BUY : TradeSide.SELL)
                .orderType(orderType)
                .tradePair(tradePair)
                .quantity(quantity)
                .price(price)
                .filled(0)
                .build();

        book.matchOrder(System.currentTimeMillis(), order);

        saveMatchEvent(MatchEvent.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(MatchEventStatus.SUBMITED)
                .topic(savematchingEngineTopic)
                .build());
    }


    private MatchEvent saveMatchEvent(MatchEvent matchEvent) {
        kafkaTemplateSendMessage.send(matchEvent.getTopic(), matchEvent.getStatus().name());
        return null;
    }
}
