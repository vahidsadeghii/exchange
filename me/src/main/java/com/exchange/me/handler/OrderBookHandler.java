package com.exchange.me.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.exchange.me.domain.TradePair;
import com.exchange.me.domain.TradeSide;
import lombok.RequiredArgsConstructor;


import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;


@RequiredArgsConstructor
public class OrderBookHandler {
    private  TradePair tradePair;
  private final TreeMap<Long, LinkedList<Order>> bids; // Descending for BUY side
  private final TreeMap<Long, LinkedList<Order>> asks; // Ascending for SELL side
  private final Map<Long, OrderLocation> orderIndex; // Fast lookup by order ID
  private long updateTime;

  // Helper class to track order location for fast cancellation
  private static class OrderLocation {
    final double price;
    final TradeSide side;
    final Order order;

    OrderLocation(double price, TradeSide side, Order order) {
      this.price = price;
      this.side = side;
      this.order = order;
    }
  }

  public OrderBookHandler(TradePair tradePair) {
    bids = new TreeMap<>(Collections.reverseOrder());
    asks = new TreeMap<>();
    orderIndex = new HashMap<>();
  }

  public List<MatchInfo> matchOrder(
      long timestamp, Order incomingOrder) {
    updateTime = timestamp;

    if (incomingOrder.getOrderSide() == TradeSide.BUY) {
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
      Map.Entry<Long, LinkedList<Order>> bestAsk = asks.firstEntry();

      long askPrice = bestAsk.getKey();

      if(askPrice > buyOrder.getPrice()) {
        break; // No more matching possible
      }

      LinkedList<Order> askQueue = bestAsk.getValue();

      while (!askQueue.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
        Order askOrder = askQueue.peek();
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
          askQueue.poll();
          orderIndex.remove(askOrder.getId()); // Remove from index
        }
      }

      // Remove empty price levels
      if (askQueue.isEmpty()) {
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
      Map.Entry<Long, LinkedList<Order>> bestBid = bids.firstEntry();
      long bidPrice = bestBid.getKey();
      LinkedList<Order> bidQueue = bestBid.getValue();

      while (!bidQueue.isEmpty() && sellOrder.getRemainingQuantity() > 0) {
        Order bidOrder = bidQueue.peek();
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
          bidQueue.poll();
          orderIndex.remove(bidOrder.getId()); // Remove from index
        }
      }

      // Remove empty price levels
      if (bidQueue.isEmpty()) {
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
    TreeMap<Long, LinkedList<Order>> book = order.getOrderSide() == TradeSide.BUY ? bids : asks;
    long priceKey = (long) order.getPrice();
    LinkedList<Order> queue = book.computeIfAbsent(priceKey, k -> new LinkedList<>());
    queue.addLast(order);
    orderIndex.put(order.getId(), new OrderLocation(order.getPrice(), order.getOrderSide(), order));
  }

  /**
   * Delete order with O(1) lookup using order index
   */
  public void deleteOrder(long timestamp, Order order) {
    updateTime = timestamp;
    OrderLocation location = orderIndex.remove(order.getId());
    
    if (location != null) {
      TreeMap<Long, LinkedList<Order>> book = location.side == TradeSide.BUY ? bids : asks;
      long priceKey = (long) location.price;
      LinkedList<Order> queue = book.get(priceKey);
      
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
  public Order getOrder(long orderId) {
    OrderLocation location = orderIndex.get(orderId);
    return location != null ? location.order : null;
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

  public long getUpdateTime() {
    return updateTime;
  }

  /**
   * Get market depth snapshot
   */
  public MarketDepth getMarketDepth(int levels) {
    List<PriceLevel> bidLevels = new ArrayList<>();
    List<PriceLevel> askLevels = new ArrayList<>();

    int count = 0;
    for (Map.Entry<Long, LinkedList<Order>> entry : bids.entrySet()) {
      if (count >= levels) break;
      double volume = entry.getValue().stream()
          .mapToDouble(Order::getRemainingQuantity)
          .sum();
      bidLevels.add(new PriceLevel(entry.getKey(), volume, entry.getValue().size()));
      count++;
    }

    count = 0;
    for (Map.Entry<Long, LinkedList<Order>> entry : asks.entrySet()) {
      if (count >= levels) break;
      double volume = entry.getValue().stream()
          .mapToDouble(Order::getRemainingQuantity)
          .sum();
      askLevels.add(new PriceLevel(entry.getKey(), volume, entry.getValue().size()));
      count++;
    }

    return new MarketDepth(bidLevels, askLevels);
  }

  // Helper classes for market depth
  public static class MarketDepth {
    private final List<PriceLevel> bids;
    private final List<PriceLevel> asks;

    public MarketDepth(List<PriceLevel> bids, List<PriceLevel> asks) {
      this.bids = bids;
      this.asks = asks;
    }

    public List<PriceLevel> getBids() { return bids; }
    public List<PriceLevel> getAsks() { return asks; }
  }

  public static class PriceLevel {
    private final long price;
    private final double volume;
    private final int orderCount;

    public PriceLevel(long price, double volume, int orderCount) {
      this.price = price;
      this.volume = volume;
      this.orderCount = orderCount;
    }

    public long getPrice() { return price; }
    public double getVolume() { return volume; }
    public int getOrderCount() { return orderCount; }
  }
}
