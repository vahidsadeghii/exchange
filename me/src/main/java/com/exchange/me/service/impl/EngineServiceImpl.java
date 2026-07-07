package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class EngineServiceImpl implements EngineService {
    private final MatchEventService matchEngineEventService;


    private final Map<TradePair, OrderBookHandler> orderBooks = new ConcurrentHashMap<>();

    @Override
    public MatchEngine createUpdateOrder(Long oldOrderId, long orderId, long userId, TradeSide tradeSide,
                                         TradePair tradePair, OrderType orderType, MarketType marketType,
                                         double quantity, double price) {

        OrderBookHandler handler = getOrCreateBook(tradePair);

        //Cancel old order
        if (oldOrderId != null) {
            Optional<Order> oldOrder = handler.getOrder(oldOrderId);
            handler.deleteOrder(System.currentTimeMillis(), oldOrder.get());

        }

        //create new order
        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .tradeSide(tradeSide)
                .orderType(orderType)
                .tradePair(tradePair)
                .marketType(marketType)
                .quantity(quantity)
                .price(price)
                .build();

        handler.matchOrder(LocalDateTime.now()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
                order);
        order.setMatchEngineStatus(MatchEventStatus.FILLED);

        return matchEngineEventService.saveMatchEvent(order);
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

    @Override
    public OrderBookHandler getOrderBook(TradePair pair) {
        return orderBooks.get(pair);
    }


    private OrderBookHandler getOrCreateBook(TradePair pair) {
        return orderBooks.computeIfAbsent(pair, p -> new OrderBookHandler(p));
    }
}