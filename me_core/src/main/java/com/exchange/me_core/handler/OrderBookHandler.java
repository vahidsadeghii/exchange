package com.exchange.me_core.handler;

import com.exchange.core.sbe.TradePair;
import com.exchange.core.sbe.TradeSide;
import com.exchange.me_core.domain.MatchInfo;
import com.exchange.me_core.domain.Order;
import com.exchange.me_core.exception.InvalidTradePairException;
import com.exchange.me_core.exception.OrderCanNotBeNullException;
import com.exchange.me_core.matching.MatchingContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;


public class OrderBookHandler {
    private final TradePair tradePair;

    @Getter
    private final TreeMap<Long, Deque<Order>> bids;
    @Getter
    private final TreeMap<Long, Deque<Order>> asks;
    private final Map<Long, OrderLocation> orderIndex;

    private final MatchingContext matchingContext;
    private final OrderHandlerFactory handlerFactory;

    private long updateTime;

    public OrderBookHandler(
            TradePair tradePair,
            OrderHandlerFactory handlerFactory) {

        if (tradePair == null) {
            throw new IllegalArgumentException("TradePair cannot be null");
        }

        if (handlerFactory == null) {
            throw new IllegalArgumentException("OrderHandlerFactory cannot be null");
        }

        this.tradePair = tradePair;
        this.handlerFactory = handlerFactory;

        this.bids = new TreeMap<>(Collections.reverseOrder());
        this.asks = new TreeMap<>();
        this.orderIndex = new HashMap<>();

        this.matchingContext =
                new MatchingContext(
                        bids,
                        asks,
                        orderIndex);

    }

    public List<MatchInfo> matchOrder(
            long timestamp,
            Order incomingOrder) {

        updateTime = timestamp;

        if (incomingOrder == null) {
            throw new OrderCanNotBeNullException();
        }

        if (!tradePair.equals(incomingOrder.getTradePair())) {
            throw new InvalidTradePairException();
        }

        return handlerFactory
                .get(incomingOrder.getOrderType())
                .execute(
                        timestamp,
                        incomingOrder,
                        matchingContext);
    }

    public void deleteOrder(long timestamp, Order order) {

        updateTime = timestamp;

        OrderLocation location = orderIndex.remove(order.getId());

        if (location == null) {
            return;
        }

        TreeMap<Long, Deque<Order>> book =
                location.side == TradeSide.BUY ? bids : asks;

        Deque<Order> queue = book.get((long) location.price);

        if (queue != null) {

            queue.remove(location.order);

            if (queue.isEmpty()) {
                book.remove((long) location.price);
            }
        }
    }

    public Optional<Order> getOrder(long orderId) {

        OrderLocation location = orderIndex.get(orderId);

        return Optional.ofNullable(
                location != null ? location.order : null);
    }

    public void reset() {

        bids.clear();
        asks.clear();
        orderIndex.clear();
    }

    public Long getBestBid() {

        return bids.isEmpty()
                ? null
                : bids.firstKey();
    }

    public Long getBestAsk() {

        return asks.isEmpty()
                ? null
                : asks.firstKey();
    }

    public Long getSpread() {

        if (bids.isEmpty() || asks.isEmpty()) {
            return null;
        }

        return asks.firstKey() - bids.firstKey();
    }

    public int getTotalOrders() {

        return orderIndex.size();
    }

    public MarketDepth getMarketDepth(int depth) {

        return new MarketDepth(
                buildLevels(bids, depth),
                buildLevels(asks, depth));
    }

    public List<PriceLevel> getBidsList(int depth) {

        return buildLevels(bids, depth);
    }

    public List<PriceLevel> getAsksList(int depth) {

        return buildLevels(asks, depth);
    }

    private List<PriceLevel> buildLevels(
            TreeMap<Long, Deque<Order>> book,
            int depth) {

        List<PriceLevel> result = new ArrayList<>();

        int count = 0;

        for (Map.Entry<Long, Deque<Order>> entry : book.entrySet()) {

            if (count++ >= depth) {
                break;
            }

            double volume = entry.getValue()
                    .stream()
                    .mapToDouble(Order::getRemainingQuantity)
                    .sum();

            result.add(new PriceLevel(
                    entry.getKey(),
                    volume,
                    entry.getValue().size()));
        }

        return result;
    }

    public record OrderLocation(
            double price,
            TradeSide side,
            Order order) {
    }

    public record MarketDepth(
            List<PriceLevel> bids,
            List<PriceLevel> asks) {
    }

    public record PriceLevel(
            long price,
            double volume,
            int orderCount) {
    }

}