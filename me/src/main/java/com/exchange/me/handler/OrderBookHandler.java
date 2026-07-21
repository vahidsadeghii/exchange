package com.exchange.me.handler;

import com.exchange.me.domain.*;

import java.util.*;

import com.exchange.me.exception.InvalidTradePairException;
import com.exchange.me.exception.OrderCanNotBeNullException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;




@Slf4j
public class OrderBookHandler {
  private final TradePair tradePair;
    private final TreeMap<Long, Deque<Order>> bids;
    private final TreeMap<Long, Deque<Order>> asks;
    private final Map<Long, OrderLocation> orderIndex;
    private final LimitOrderService limitOrderService;
    private final MarketOrderService marketOrderService;
    private final FokOrderService fokOrderService;

    private long updateTime;

    private static final int QUEUE_INITIAL_CAPACITY = 100;


    public OrderBookHandler(TradePair tradePair, OrderMatchingUtility orderMatchingUtility) {
        if (tradePair == null) {
            throw new IllegalArgumentException("TradePair cannot be null");
        }
        if (orderMatchingUtility == null) {
            throw new IllegalArgumentException("OrderMatchingUtility cannot be null");
        }

        this.tradePair = tradePair;
        this.bids = new TreeMap<>(Collections.reverseOrder());  // Higher prices first
        this.asks = new TreeMap<>();                             // Lower prices first
        this.orderIndex = new HashMap<>();

        // Initialize services with OrderMatchingUtility
        this.limitOrderService = new LimitOrderService(orderMatchingUtility);
        this.marketOrderService = new MarketOrderService(orderMatchingUtility);
        this.fokOrderService = new FokOrderService(orderMatchingUtility);

        log.debug("OrderBookHandler created for pair: {}", tradePair);
    }

    /**
     * Main entry point - matches an incoming order against the current order book.
     * Delegates to appropriate service based on order type.
     */
    public List<MatchInfo> matchOrder(long timestamp, Order incomingOrder) {
        updateTime = timestamp;

        if (incomingOrder == null) {
            throw new OrderCanNotBeNullException();
        }

        if (!incomingOrder.getTradePair().equals(this.tradePair)) {
            throw new InvalidTradePairException();
        }

        return switch (incomingOrder.getOrderType()) {
            case LIMIT -> limitOrderService.execute(
                    timestamp,
                    incomingOrder,
                    bids,
                    asks,
                    orderIndex,
                    this::addOrderToBook);

            case MARKET -> marketOrderService.execute(
                    timestamp,
                    incomingOrder,
                    bids,
                    asks,
                    orderIndex);

            case FOK -> fokOrderService.execute(
                    timestamp,
                    incomingOrder,
                    bids,
                    asks,
                    orderIndex);

            default -> throw new IllegalArgumentException(
                    "Unsupported order type: " + incomingOrder.getOrderType()
            );
        };
    }

    /**
     * Adds an order to the order book at the appropriate price level.
     */
    public void addOrderToBook(Order order) {
        TreeMap<Long, Deque<Order>> book = order.getTradeSide() == TradeSide.BUY ? bids : asks;
        long priceKey = (long) order.getPrice();
        Deque<Order> queue = book.computeIfAbsent(priceKey, k -> new ArrayDeque<>(QUEUE_INITIAL_CAPACITY));
        queue.addLast(order);
        orderIndex.put(order.getId(),
                new OrderLocation(order.getPrice(), order.getTradeSide(), order));
        log.debug("Order {} added to {} book at price {}", order.getId(), order.getTradeSide(), priceKey);
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
                queue.remove(location.order);
                if (queue.isEmpty()) {
                    book.remove(priceKey);
                }
                log.debug("Order {} deleted from {} book at price {}", order.getId(), location.side, priceKey);
            }
        } else {
            log.warn("Attempted to delete non-existent order:{}", order.getId());
        }
    }

    /**
     * Get order by ID with O(1) lookup
     */
    public Optional<Order> getOrder(long orderId) {
        OrderLocation location = orderIndex.get(orderId);
        return Optional.ofNullable(location != null ? location.order : null);
    }

    public int getTotalOrders() {
        return orderIndex.size();
    }

    public Long getBestBid() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    public Long getBestAsk() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

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

    public MarketDepth getMarketDepth(int levels) {
        if (levels <= 0) {
            throw new IllegalArgumentException("Market depth levels must be positive");
        }
        List<PriceLevel> bidLevels = buildLevels(bids, levels);
        List<PriceLevel> askLevels = buildLevels(asks, levels);
        return new MarketDepth(bidLevels, askLevels);
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
            result.add(new PriceLevel(entry.getKey(), volume, entry.getValue().size()));
            count++;
        }
        return result;
    }

    // Helper records
    public record OrderLocation(double price, TradeSide side, Order order) {}
    public record MarketDepth(List<PriceLevel> bids, List<PriceLevel> asks) {}
    public record PriceLevel(long price, double volume, int orderCount) {}
}