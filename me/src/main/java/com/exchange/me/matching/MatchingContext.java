package com.exchange.me.matching;


import com.exchange.me.sbe.TradeSide;
import com.exchange.me.domain.Order;
import com.exchange.me.handler.OrderBookHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;


public class MatchingContext {

    private static final int QUEUE_INITIAL_CAPACITY = 100;

    private final TreeMap<Long, Deque<Order>> bids;
    private final TreeMap<Long, Deque<Order>> asks;
    private final Map<Long, OrderBookHandler.OrderLocation> orderIndex;

    public MatchingContext(
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        this.bids = bids;
        this.asks = asks;
        this.orderIndex = orderIndex;
    }

    public TreeMap<Long, Deque<Order>> getBids() {
        return bids;
    }

    public TreeMap<Long, Deque<Order>> getAsks() {
        return asks;
    }

    public Map<Long, OrderBookHandler.OrderLocation> getOrderIndex() {
        return orderIndex;
    }

    public void addOrder(Order order) {

        TreeMap<Long, Deque<Order>> book =
                order.getTradeSide() == TradeSide.BUY ? bids : asks;

        long price = (long) order.getPrice();

        Deque<Order> queue =
                book.computeIfAbsent(price,
                        p -> new ArrayDeque<>(QUEUE_INITIAL_CAPACITY));

        queue.addLast(order);

        orderIndex.put(order.getId(),
                new OrderBookHandler.OrderLocation(
                        order.getPrice(),
                        order.getTradeSide(),
                        order));
    }
}