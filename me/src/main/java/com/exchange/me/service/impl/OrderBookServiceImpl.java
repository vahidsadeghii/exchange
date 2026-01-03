package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
public class OrderBookServiceImpl implements OrderBookService {
    private final EngineService engineService;

    private final Map<TradePair, OrderBookHandler> orderBooks = new ConcurrentHashMap<>();

    private OrderBookHandler getOrCreateBook(TradePair pair) {
        return orderBooks.computeIfAbsent(pair, p -> new OrderBookHandler(p));
    }

    @Override
    public List<MatchInfo> createNewOrder(long orderId, long userId,
                                          TradeSide tradeSide, TradePair tradePair,
                                          OrderType orderType, double quantity, double price) {

        OrderBookHandler handler = getOrCreateBook(tradePair);

        List<MatchInfo> matchInfos = handler.matchOrder(LocalDateTime.now()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
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

    @Override
    public void deleteOrder(long timestamp, Order order) {
        OrderBookHandler handler = orderBooks.get(order.getTradePair());
        if (handler != null) {
            handler.deleteOrder(timestamp, order);
        }
    }

    @Override
    public Order getOrder(TradePair pair, long orderId) {
        if (pair == null) {
            throw new InvalidTradPairException();
        }
        OrderBookHandler handler = orderBooks.get(pair);
        if (handler == null) {
            throw new NotFoundOrderBookHandlerException();
        }

        return handler.getOrder(orderId).orElseThrow();
    }

    @Override
    public OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels) {
        OrderBookHandler handler = orderBooks.get(pair);
        return handler != null ? handler.getMarketDepth(levels) : null;
    }

    @Override
    public void resetAll() {
        orderBooks.values().forEach(OrderBookHandler::reset);
    }

}
