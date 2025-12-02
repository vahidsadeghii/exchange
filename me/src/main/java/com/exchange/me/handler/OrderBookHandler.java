package com.exchange.me.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import com.exchange.me.domain.OrderSide;
import org.springframework.stereotype.Component;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;

@Component
public class OrderBookHandler {
  private final TreeMap<Long, Queue<Order>> bids; // Descending for BUY side
  private final TreeMap<Long, Queue<Order>> asks; // Ascending for SELL side
  private long updateTime;

  public OrderBookHandler() {
    bids = new TreeMap<>(Collections.reverseOrder());
    asks = new TreeMap<>();
  }

  public List<MatchInfo> matchOrder(
      long timestamp, Order incomingOrder) {
    updateTime = timestamp;

    if (incomingOrder.getOrderSide() == OrderSide.BUY) {
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
    while (!asks.isEmpty() && buyOrder.getRemainingQuantity()> 0) {
      // Get the best (lowest) ask price level
      Map.Entry<Long, Queue<Order>> bestAsk = asks.firstEntry();

      long askPrice = bestAsk.getKey();

      if(askPrice > buyOrder.getPrice()) {
        break; // No more matching possible
      }

      Queue<Order> askQueue = bestAsk.getValue();

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
                OrderSide.SELL,
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
        }
      }

      // Remove empty price levels
      if (askQueue.isEmpty()) {
        asks.pollFirstEntry();
      }
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
      Map.Entry<Long, Queue<Order>> bestBid = bids.firstEntry();
      long bidPrice = bestBid.getKey();
      Queue<Order> bidQueue = bestBid.getValue();

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
                OrderSide.BUY,
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
        }
      }

      // Remove empty price levels
      if (bidQueue.isEmpty()) {
        bids.pollFirstEntry();
      }
    }

    return matches;
  }

  public void deleteOrder(long timestamp, Order order) {
    updateTime = timestamp;
    TreeMap<Long, Queue<Order>> book = order.getOrderSide() == OrderSide.BUY ? bids : asks;
    Queue<Order> queue = book.get(order.getPrice());
    if (queue != null) {
      queue.removeIf(o -> o.getId() == order.getId());
      if (queue.isEmpty()) {
        book.remove(order.getPrice());
      }
    }
  }

  public void reset() {
    bids.clear();
    asks.clear();
  }

  public long getUpdateTime() {
    return updateTime;
  }
}
