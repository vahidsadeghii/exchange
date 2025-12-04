package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.MatchEngineEventService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EngineServiceImpl implements EngineService {
    private final OrderBookHandler orderBookHandler;
    private final MatchEngineEventService matchEngineEventService;

    @Value("${custom-config.kafka.updatematchingEngine-output-message.topic}")
    private String updatematchingEngineTopic;

    @Value("${custom-config.kafka.savematchingEngine-output-message.topic}")
    private String savematchingEngineTopic;

    @Override
    public void processOrder(long orderId, long userId, TradePair tradePair, OrderType orderType,
                             boolean isBuyOrder, double quantity,
                             double price) {
        orderBookHandler.matchOrder(System.currentTimeMillis(),
                Order.builder()
                        .id(orderId)
                        .timestamp(System.currentTimeMillis())
                        .userId(userId)
                        .orderSide(isBuyOrder ? OrderSide.BUY : OrderSide.SELL)
                        .orderType(orderType)
                        .tradePair(tradePair)
                        .quantity(quantity)
                        .price(price)
                        .filled(0)
                        .build()
        );

        matchEngineEventService.saveMatchEngineEvent(MatchEngineEvent.builder()
                .id(orderId)
                .userId(userId)
                .status(MatchEngineEventStatus.SUCCESS)
                .topic(savematchingEngineTopic)
                .build());
    }

    @Override
    public void UpdateMatchInfo(long orderId, TradePair tradePair, OrderType orderType, double quantity, double price) {
//        matchEngineEventService.updateMatchEngineEvent(MatchEngineEvent.builder()
//                .id(orderId)
//                .status(MatchEngineEventStatus.SUCCESS)
//                .topic(updatematchingEngineTopic)
//                .build());
    }
}
