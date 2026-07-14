package com.exchange.me.handler;

import com.exchange.me.domain.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.exchange.me.exception.InvalidTradePairException;
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


    public OrderBookHandler(TradePair tradePair) {
        this.tradePair = tradePair;

        // BUY: highest price first
        bids = new TreeMap<>(Collections.reverseOrder());

        // SELL: lowest price first
        asks = new TreeMap<>();
        orderIndex = new HashMap<>();
    }

    /**
     * Matches an incoming order against the current order book.
     *
     * <p>This method is the main entry point of the matching engine. It validates
     * that the order belongs to the current trading pair and delegates the matching
     * process based on the order type and trade side.</p>
     *
     * <p>Supported order types:</p>
     * <ul>
     *     <li>
     *         LIMIT:
     *         Matches against available liquidity at acceptable prices. Any
     *         remaining quantity may be added to the order book.
     *     </li>
     *     <li>
     *         MARKET:
     *         Executes immediately against the best available prices without
     *         considering a limit price.
     *     </li>
     *     <li>
     *         FOK (Fill Or Kill):
     *         Executes only if the entire order quantity can be filled immediately.
     *         If full execution is not possible, the order is cancelled and no
     *         partial execution occurs.
     *     </li>
     * </ul>
     *
     * @param timestamp     the matching event timestamp
     * @param incomingOrder the order submitted to the matching engine
     * @return a list of executed matches generated from this order.
     * Returns an empty list when the order cannot be executed
     * (for example, an unfillable FOK order).
     * @throws InvalidTradePairException if the order trading pair does not
     *                                   match this order book's trading pair
     */
    public List<MatchInfo> matchOrder(
            long timestamp,
            Order incomingOrder) {

        updateTime = timestamp;


        if (!incomingOrder.getTradePair().equals(this.tradePair)) {
            throw new InvalidTradePairException();
        }

        /**
         * FOK (Fill Or Kill) orders require a limit price because
         * the engine must verify that the entire order quantity can
         * be filled within the acceptable price range before execution.
         *
         * <p>A FOK order without a valid price cannot determine whether
         * full execution is possible.</p>
         */
        if (incomingOrder.getOrderType() == OrderType.FOK
                && incomingOrder.getPrice() <= 0) {
            throw new IllegalArgumentException(
                    "FOK order requires a valid limit price"
            );
        }

        return switch (incomingOrder.getOrderType()) {

            case LIMIT, MARKET -> incomingOrder.getTradeSide() == TradeSide.BUY
                    ? executeBuyOrder(timestamp, incomingOrder)
                    : executeSellOrder(timestamp, incomingOrder);

            case FOK -> executeFokOrder(timestamp, incomingOrder);


            default -> throw new IllegalArgumentException(
                    "Unsupported order type: "
                            + incomingOrder.getOrderType()
            );
        };
    }

    // BUY SIDE
    public List<MatchInfo> executeBuyOrder(long timestamp, Order buyOrder) {
        List<MatchInfo> matches = new ArrayList<>();

        // Only process the best ask levels until order is filled
        while (!asks.isEmpty() && buyOrder.getRemainingQuantity() > 0) {

            // Get the best (lowest) ask price level
            Map.Entry<Long, Deque<Order>> bestAsk = asks.firstEntry();
            if (bestAsk == null) {
                break;
            }

            long askPrice = bestAsk.getKey();

            /**
             * Stops matching when the best available ask price is higher than
             * the maximum price accepted by a LIMIT BUY order.
             *
             * <p>MARKET orders ignore price limits and continue matching against
             * available liquidity. LIMIT orders only execute trades at prices
             * equal to or better than their specified limit price.</p>
             */
            if (buyOrder.getOrderType() == OrderType.LIMIT &&
                    askPrice > buyOrder.getPrice()) {
                break;
            }


            Deque<Order> askList = bestAsk.getValue();

            while (!askList.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
                Order askOrder = askList.peek();
                if (askOrder == null)
                    break;

                double tradedQuantity = Math.min(
                        buyOrder.getRemainingQuantity(),
                        askOrder.getRemainingQuantity()
                );

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

        /**
         * Adds the remaining quantity of a LIMIT BUY order to the order book.
         *
         * <p>If the order could not be fully matched and still has remaining
         * quantity, it is stored in the bid side of the book. MARKET orders
         * are never added because they must execute immediately or expire.</p>
         */
        if (buyOrder.getOrderType() == OrderType.LIMIT &&
                buyOrder.getRemainingQuantity() > 0) {
            addOrderToBook(buyOrder);
        }

        return matches;
    }

    // SELL SIDE
    public List<MatchInfo> executeSellOrder(long timestamp, Order sellOrder) {
        List<MatchInfo> matches = new ArrayList<>();

        // Only process the best bid levels until order is filled
        while (!bids.isEmpty() && sellOrder.getRemainingQuantity() > 0) {

            // Get the best (highest) bid price level
            Map.Entry<Long, Deque<Order>> bestBid = bids.firstEntry();
            long bidPrice = bestBid.getKey();
            Deque<Order> bidList = bestBid.getValue();

            /**
             * Stops matching when the best available bid price is lower than
             * the minimum price accepted by a LIMIT SELL order.
             *
             * <p>A LIMIT SELL order can only match with buyers offering a price
             * equal to or higher than the seller's limit price.</p>
             */
            if (sellOrder.getOrderType() == OrderType.LIMIT &&
                    bidPrice < sellOrder.getPrice()) {
                break;
            }

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

        /**
         * Adds the remaining quantity of a LIMIT SELL order to the order book.
         *
         * <p>If the order is partially filled and still has remaining quantity,
         * it is placed on the ask side of the order book for future matching.</p>
         */
        if (sellOrder.getOrderType() == OrderType.LIMIT &&
                sellOrder.getRemainingQuantity() > 0) {
            addOrderToBook(sellOrder);
        }

        return matches;
    }

    private List<MatchInfo> executeFokOrder(long timestamp, Order order) {
        if (!canFullyFill(order)) {
            return List.of();
        }

        return order.getTradeSide() == TradeSide.BUY
                ? executeBuyOrder(timestamp, order)
                : executeSellOrder(timestamp, order);
    }

    // ADD ORDER TO BOOK(for unfilled orders after matching)
    private void addOrderToBook(Order order) {
        TreeMap<Long, Deque<Order>> book = order.getTradeSide() == TradeSide.BUY ? bids : asks;

        long priceKey = (long) order.getPrice();
        Deque<Order> queue = book.computeIfAbsent(
                priceKey, k -> new ArrayDeque<>(100));
        queue.addLast(order);

        orderIndex.put(order.getId(),
                new OrderLocation(order.getPrice(), order.getTradeSide(), order));
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

    // Helper class to track order location for fast cancellation
    public record OrderLocation(double price, TradeSide side, Order order) {
    }

    // Helper classes for market depth
    public record MarketDepth(List<PriceLevel> bids, List<PriceLevel> asks) {
    }

    // Helper classes for price level
    public record PriceLevel(long price, double volume, int orderCount) {
    }


    public List<PriceLevel> getBidsList(int depth) {
        return buildLevels(bids, depth);

    }

    public List<PriceLevel> getAsksList(int depth) {
        return buildLevels(asks, depth);
    }

    private List<PriceLevel> buildLevels(TreeMap<Long, Deque<Order>> book, int depth) {
        List<PriceLevel> result = new ArrayList<>();
        int count = 0;

        for (Map.Entry<Long, Deque<Order>> entry : book.entrySet()) {
            if (count >= depth) {
                break;
            }

            double volume = entry.getValue().stream()
                    .mapToDouble(Order::getRemainingQuantity)
                    .sum();

            result.add(new PriceLevel(
                    entry.getKey(),
                    volume,
                    entry.getValue().size()
            ));

            count++;
        }

        return result;
    }

    private boolean canFullyFill(Order order) {

        if (order.getOrderType() == OrderType.FOK
                && order.getPrice() <= 0) {
            throw new IllegalArgumentException(
                    "FOK order requires a valid limit price"
            );
        }
        double availableQuantity = 0;

        if (order.getTradeSide() == TradeSide.BUY) {
            for (Map.Entry<Long, Deque<Order>> entry : asks.entrySet()) {
                long askPrice = entry.getKey();

                // Price is too expensive
                if (askPrice > order.getPrice()) {
                    break;
                }

                for (Order ask : entry.getValue()) {

                    availableQuantity += ask.getRemainingQuantity();

                    if (availableQuantity >= order.getRemainingQuantity()) {
                        return true;
                    }
                }
            }
        } else {
            for (Map.Entry<Long, Deque<Order>> entry : bids.entrySet()) {
                long bidPrice = entry.getKey();

                // Price is too low
                if (bidPrice < order.getPrice()) {
                    break;
                }

                for (Order bid : entry.getValue()) {
                    availableQuantity += bid.getRemainingQuantity();

                    if (availableQuantity >= order.getRemainingQuantity()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
