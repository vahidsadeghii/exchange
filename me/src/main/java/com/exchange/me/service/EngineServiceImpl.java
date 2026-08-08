package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EngineServiceImpl {
  private final Map<TradePair, OrderBookHandler> orderBooks = new ConcurrentHashMap<>();

  public MatchEngine createUpdateOrder(
      Long oldOrderId,
      long orderId,
      long userId,
      TradeSide tradeSide,
      TradePair tradePair,
      OrderType orderType,
      MarketType marketType,
      double quantity,
      double price) {

    OrderBookHandler handler = getOrCreateBook(tradePair);

    // Cancel old order
    if (oldOrderId != null) {
      Optional<Order> oldOrder = handler.getOrder(oldOrderId);
      handler.deleteOrder(System.currentTimeMillis(), oldOrder.get());
    }

    // create new order
    Order order =
        Order.builder()
            .id(orderId)
            .userId(userId)
            .tradeSide(tradeSide)
            .orderType(orderType)
            .tradePair(tradePair)
            .marketType(marketType)
            .quantity(quantity)
            .price(price)
            .build();

    handler.matchOrder(
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), order);

    order.setMatchEngineStatus(MatchEventStatus.FILLED);

    return null;
  }

  public void deleteOrder(long timestamp, Order order) {
    OrderBookHandler handler = orderBooks.get(order.getTradePair());
    if (handler != null) {
      handler.deleteOrder(timestamp, order);
    }
  }

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

  public OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels) {
    OrderBookHandler handler = orderBooks.get(pair);
    return handler != null ? handler.getMarketDepth(levels) : null;
  }

  public void resetAll() {
    orderBooks.values().forEach(OrderBookHandler::reset);
  }

  public OrderBookHandler getOrderBook(TradePair pair) {
    return orderBooks.get(pair);
  }

  public OrderBookDepth getOrderBookDepth(TradePair pair, int depth) {
    OrderBookHandler book = getOrderBook(pair);

    List<PriceLevel> bids =
        book.getBidsList(depth).stream()
            .map(level -> new PriceLevel(level.price(), level.volume(), level.orderCount()))
            .toList();

    List<PriceLevel> asks =
        book.getAsksList(depth).stream()
            .map(level -> new PriceLevel(level.price(), level.volume(), level.orderCount()))
            .toList();

    return new OrderBookDepth(bids, asks);
  }

  private OrderBookHandler getOrCreateBook(TradePair pair) {
    return orderBooks.computeIfAbsent(pair, p -> new OrderBookHandler(p));
  }
}
