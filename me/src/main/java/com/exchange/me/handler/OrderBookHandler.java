package com.exchange.me.handler;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class OrderBookHandler {
    private TradePair tradePair;
    private final TreeMap<Long, Deque<Order>> bids; // Descending for BUY side
    private final TreeMap<Long, Deque<Order>> asks; // Ascending for SELL side
    private final Map<Long, OrderLocation> orderIndex; // Fast lookup by order ID
    private long updateTime;

    // Helper class to track order location for fast cancellation
    private record OrderLocation(double price, TradeSide side, Order order) {
    }

    // Helper classes for market depth
    public record MarketDepth(List<PriceLevel> bids, List<PriceLevel> asks) {
    }

    // Helper classes for price level
    public record PriceLevel(long price, double volume, int orderCount) {
    }

    public OrderBookHandler(TradePair tradePair) {
        this.tradePair = tradePair;
        bids = new TreeMap<>(Collections.reverseOrder());
        asks = new TreeMap<>();
        orderIndex = new HashMap<>();
    }

    public List<MatchInfo> matchOrder(
            long timestamp, Order incomingOrder) {
        updateTime = timestamp;
        if (!incomingOrder.getTradePair().equals(this.tradePair)) {
            throw new IllegalArgumentException("Order pair " + incomingOrder.getTradePair() +
                    " does not match handler pair " + this.tradePair);
        }
        if (incomingOrder.getTradeSide() == TradeSide.BUY) {
            return executeBuyOrder(timestamp, incomingOrder);
        } else {
            return executeSellOrder(timestamp, incomingOrder);
        }
    }

    public List<MatchInfo> executeBuyOrder(
            long timestamp,
            Order buyOrder) {

        List<MatchInfo> matches = new ArrayList<>();
        // Only process the best ask levels until order is filled
        while (!asks.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
            // Get the best (lowest) ask price level
            Map.Entry<Long, Deque<Order>> bestAsk = asks.firstEntry();

            long askPrice = bestAsk.getKey();

            if (askPrice > buyOrder.getPrice()) {
                break; // No more matching possible
            }

            Deque<Order> askList = bestAsk.getValue();

            while (!askList.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
                Order askOrder = askList.peek();
                if (askOrder == null)
                    break;

                double tradedQuantity = Math.min(buyOrder.getRemainingQuantity(), askOrder.getRemainingQuantity());
                buyOrder.setFilled(buyOrder.getFilled() + tradedQuantity);
                askOrder.setFilled(askOrder.getFilled() + tradedQuantity);

                matches.add(
                        new MatchInfo(
                                timestamp,
                                System.currentTimeMillis(),
                                TradeSide.SELL,
                                buyOrder.getId(),
                                askOrder.getId(),
                                buyOrder.getUserId(),
                                askOrder.getUserId(),
                                tradedQuantity,
                                askPrice,
                                buyOrder.getQuantity(),
                                buyOrder.getRemainingQuantity(),
                                askOrder.getQuantity(),
                                askOrder.getRemainingQuantity()));

                if (askOrder.getRemainingQuantity() == 0) {
                    askList.poll();
                    orderIndex.remove(askOrder.getId()); // Remove from index
                }
            }

            // Remove empty price levels
            if (askList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }

        // Add unfilled buy order to book
        if (buyOrder.getRemainingQuantity() > 0) {
            addOrderToBook(buyOrder);
        }

        return matches;
    }

    public List<MatchInfo> executeSellOrder(
            long timestamp,
            Order sellOrder) {
        List<MatchInfo> matches = new ArrayList<>();

        // Only process the best bid levels until order is filled
        while (!bids.isEmpty() && sellOrder.getRemainingQuantity() > 0) {
            // Get the best (highest) bid price level
            Map.Entry<Long, Deque<Order>> bestBid = bids.firstEntry();
            long bidPrice = bestBid.getKey();
            Deque<Order> bidList = bestBid.getValue();

            while (!bidList.isEmpty() && sellOrder.getRemainingQuantity() > 0) {
                Order bidOrder = bidList.peek();
                if (bidOrder == null)
                    break;

                double tradedQuantity = Math.min(sellOrder.getRemainingQuantity(), bidOrder.getRemainingQuantity());
                sellOrder.setFilled(sellOrder.getFilled() + tradedQuantity);
                bidOrder.setFilled(bidOrder.getFilled() + tradedQuantity);

                matches.add(
                        new MatchInfo(timestamp,
                                System.currentTimeMillis(),
                                TradeSide.BUY,
                                sellOrder.getId(),
                                bidOrder.getId(),
                                sellOrder.getUserId(),
                                bidOrder.getUserId(),
                                tradedQuantity,
                                bidPrice,
                                sellOrder.getQuantity(),
                                sellOrder.getRemainingQuantity(),
                                bidOrder.getQuantity(),
                                bidOrder.getRemainingQuantity()));

                if (bidOrder.getRemainingQuantity() == 0) {
                    bidList.poll();
                    orderIndex.remove(bidOrder.getId()); // Remove from index
                }
            }

            // Remove empty price levels
            if (bidList.isEmpty()) {
                bids.pollFirstEntry();
            }
        }

        // Add unfilled sell order to book
        if (sellOrder.getRemainingQuantity() > 0) {
            addOrderToBook(sellOrder);
        }

        return matches;
    }

    /**
     * Add order to the book (for unfilled orders after matching)
     */
    private void addOrderToBook(Order order) {
        TreeMap<Long, Deque<Order>> book = order.getTradeSide() == TradeSide.BUY ? bids : asks;
        long priceKey = (long) order.getPrice();
        Deque<Order> queue = book.computeIfAbsent(priceKey, k -> new ArrayDeque<>(100));
        queue.addLast(order);
        orderIndex.put(order.getId(), new OrderLocation(order.getPrice(), order.getTradeSide(), order));
    }

    /**
     * Delete order with O(1) lookup using order index
     */
    public void deleteOrder(long timestamp, Order order) {
        updateTime = timestamp;
        OrderLocation location = orderIndex.remove(order.getId());

        if (location != null) {
            TreeMap<Long, Deque<Order>> book = location.side == TradeSide.BUY ? bids : asks;
            long priceKey = (long) location.price;
            Deque<Order> queue = book.get(priceKey);

            if (queue != null) {
                queue.remove(location.order); // O(n) but on LinkedList it's faster than Queue
                if (queue.isEmpty()) {
                    book.remove(priceKey);
                }
            }
        }
    }

    /**
     * Get order by ID with O(1) lookup
     */
    public Optional<Order> getOrder(long orderId) {
        OrderLocation location = orderIndex.get(orderId);
        return Optional.ofNullable(location != null ? location.order : null);
    }

    /**
     * Get total number of orders in the book
     */
    public int getTotalOrders() {
        return orderIndex.size();
    }

    /**
     * Get best bid price (highest buy price)
     */
    public Long getBestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    /**
     * Get best ask price (lowest sell price)
     */
    public Long getBestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    /**
     * Get spread (difference between best ask and best bid)
     */
    public Long getSpread() {
        Long bestBid = getBestBid();
        Long bestAsk = getBestAsk();
        return (bestBid != null && bestAsk != null) ? bestAsk - bestBid : null;
    }

    public void reset() {
        bids.clear();
        asks.clear();
        orderIndex.clear();
    }

    /**
     * Get market depth snapshot
     */
    public MarketDepth getMarketDepth(int levels) {
        List<PriceLevel> bidLevels;
        List<PriceLevel> askLevels;
        bidLevels = queueDepth(levels, bids);
        askLevels = queueDepth(levels, asks);

        return new MarketDepth(bidLevels, askLevels);
    }

    private List<PriceLevel> queueDepth(int levels, TreeMap<Long, Deque<Order>> orders) {
        List<PriceLevel> levelList = new ArrayList<>();
        int count = 0;

        for (Map.Entry<Long, Deque<Order>> entry : orders.entrySet()) {
            if (count >= levels) break;
            double volume = entry.getValue().stream()
                    .mapToDouble(Order::getRemainingQuantity)
                    .sum();
            levelList.add(new PriceLevel(entry.getKey(), volume, entry.getValue().size()));
            count++;
        }

        return levelList;
    }
}
