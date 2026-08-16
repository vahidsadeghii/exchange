package com.exchange.me.handler;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderHandlerFactory {
    private final Map<OrderType, OrderHandler> handlers;

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
