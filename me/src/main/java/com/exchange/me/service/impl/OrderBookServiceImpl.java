package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class OrderBookServiceImpl implements OrderBookService {
    private final EngineService engineService;

    private final Map<TradePair, OrderBookHandler> books = new ConcurrentHashMap<>();

    private OrderBookHandler getOrCreateBook(TradePair pair) {
        return books.computeIfAbsent(pair, p -> new OrderBookHandler(p));
    }

    @Override
    public List<MatchInfo> createNewOrder(long timestamp, long orderId, long userId,
                                          TradeSide tradeSide, TradePair tradePair,
                                          OrderType orderType, double quantity, double price) {

        OrderBookHandler handler = getOrCreateBook(tradePair);

        List<MatchInfo> matchInfos = handler.matchOrder(timestamp,
                Order.builder()
                        .id(orderId)
                        .userId(userId)
                        .tradeSide(tradeSide)
                        .orderType(orderType)
                        .tradePair(tradePair)
                        .quantity(quantity)
                        .price(price)
                        .build());

        engineService.processOrder(orderId, userId, tradePair, orderType, tradeSide, quantity, price);

        return matchInfos;
    }


//    public OrderBookHandler getBook(TradePair pair) {
//        if (pair == null) {
//            throw new InvalidTradPairException();
//        }
//        OrderBookHandler handler = books.get(pair);
//        if (handler == null) {
//            throw new NotFoundOrderBookHandlerException();
//        }
//        return handler;
//    }


}
