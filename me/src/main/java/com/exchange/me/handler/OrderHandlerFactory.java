package com.exchange.me.handler;

import com.exchange.core.sbe.OrderType;
import com.exchange.me.matching.MatchingEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderHandlerFactory {
    private final Map<OrderType, OrderHandler> handlers;

    public static OrderHandlerFactory createFactory() {
        return new OrderHandlerFactory(
                Arrays.asList(
                        new FokOderHandler(new MatchingEngine()),
                        new LimitOrderHandler(new MatchingEngine()),
                        new MarketOrderHandler(new MatchingEngine())
                )
        );
    }

    public OrderHandlerFactory(List<OrderHandler> handlers) {

        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        OrderHandler::supports,
                        Function.identity()));
    }

    public OrderHandler get(OrderType type) {

        OrderHandler handler = handlers.get(type);

        if (handler == null) {
            throw new IllegalArgumentException(
                    "Unsupported order type : " + type);
        }

        return handler;
    }
}
