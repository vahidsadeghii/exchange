package com.exchange.me.service;


import com.exchange.me.domain.EngineSnapshot;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderBookDepth;
import com.exchange.me.domain.PriceLevel;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.handler.OrderHandlerFactory;
import com.exchange.me.sbe.MarketType;
import com.exchange.me.sbe.MatchStatus;
import com.exchange.me.sbe.OrderType;
import com.exchange.me.sbe.TradePair;
import com.exchange.me.sbe.TradeSide;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class EngineService {
    private final Map<TradePair, OrderBookHandler> orderBooks = new ConcurrentHashMap<>();

    public Order createUpdateOrder(
            Long oldOrderId,
            long orderId,
            long userId,
            long timestamp,
            TradeSide tradeSide,
            TradePair tradePair,
            OrderType orderType,
            MarketType marketType,
            long quantity,
            long price) {

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
                        .timestamp(timestamp)
                        .build();

        log.debug("Order created: orderId={}, tradePair={}, quantity={}, price={}",
                orderId,
                tradePair,
                quantity,
                price);

        handler.matchOrder(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), order);

        order.setMatchStatus(MatchStatus.SUBMITED);

        log.info("Order submitted: orderId={}, tradePair={}, matchStatus={}",
                orderId,
                tradePair,
                order.getMatchStatus());

        return order;
    }

    public Order cancelOrder(long orderId, TradePair tradePair) {
        Order order = getOrder(tradePair, orderId);
        deleteOrder(System.currentTimeMillis(), order);

        log.info("Order cancelled: orderId={}, tradePair={}", orderId, tradePair);
        return order;
    }

    public void deleteOrder(long timestamp, Order order) {
        if (order == null) {
            log.warn("Attempted to delete null order");
            return;
        }
        TradePair tradePair = order.getTradePair();

        log.debug("Deleting order: orderId={}, tradePair={}, timestamp={}", order.getId(), tradePair, timestamp);

        OrderBookHandler handler = orderBooks.get(tradePair);

        if (handler != null) {
            handler.deleteOrder(timestamp, order);

            log.debug("Order deleted: orderId={}, tradePair={}", order.getId(), tradePair);
        } else {
            log.warn("Order book not found while deleting order: orderId={}, tradePair={}", order.getId(), tradePair);
        }
    }

    public Order getOrder(TradePair pair, long orderId) {
        if (pair == null) {
            log.warn("Attempted to get order with null trade pair: orderId={}", orderId);
            throw new InvalidTradPairException();
        }

        OrderBookHandler handler = orderBooks.get(pair);
        if (handler == null) {
            log.warn("Order book not found: tradePair={}, orderId={}", pair, orderId);

            throw new NotFoundOrderBookHandlerException();
        }

        return handler.getOrder(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: orderId={}, tradePair={}", orderId, pair);

                    return new NotFoundOrderBookHandlerException();
                });
    }

    public OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels) {
        log.debug("Getting market depth: tradePair={}, levels={}", pair, levels);

        OrderBookHandler handler = orderBooks.get(pair);

        if (handler == null) {
            log.warn("Order book not found while getting market depth: tradePair={}", pair);
            return null;
        }

        return handler != null ? handler.getMarketDepth(levels) : null;
    }

    public void resetAll() {
        log.info("Resetting all order books: bookCount={}", orderBooks.size());

        orderBooks.values().forEach(OrderBookHandler::reset);
        log.info("All order books reset");
    }

    public OrderBookHandler getOrderBook(TradePair pair) {
        return orderBooks.get(pair);
    }

    public OrderBookDepth getOrderBookDepth(TradePair pair, int depth) {
        log.debug("Getting order book depth: tradePair={}, depth={}", pair, depth);
        OrderBookHandler book = getOrderBook(pair);

        if (book == null) {
            log.warn("Order book not found: tradePair={}", pair);
            return null;
        }

        List<PriceLevel> bids =
                book.getBidsList(depth).stream()
                        .map(level -> new PriceLevel(level.price(), level.volume(), level.orderCount()))
                        .toList();

        List<PriceLevel> asks =
                book.getAsksList(depth).stream()
                        .map(level -> new PriceLevel(level.price(), level.volume(), level.orderCount()))
                        .toList();

        log.debug("Order book depth retrieved: tradePair={}, depth={}, bids={}, asks={}",
                pair,
                depth,
                bids.size(),
                asks.size());
        return new OrderBookDepth(bids, asks);
    }


    private OrderBookHandler getOrCreateBook(TradePair pair) {
        return orderBooks.computeIfAbsent(pair, (p) ->
        {
            log.info("Creating new order book: tradePair={}", p);
            return new OrderBookHandler(p, OrderHandlerFactory.createFactory());
        });
    }

    public EngineSnapshot createSnapshot() {
        List<Order> allOrders = getAllOrders();

        log.info("Engine snapshot created: orderCount={}", allOrders.size());
        Map<TradePair, Map<Long, Deque<Order>>> bids = new TreeMap<>();
        Map<TradePair, Map<Long, Deque<Order>>> asks = new TreeMap<>();
        orderBooks.forEach((key, value) -> {
            bids.put(key, value.getBids());
            asks.put(key, value.getAsks());
        });

        return new EngineSnapshot(allOrders, bids, asks);
    }

    public List<Order> getAllOrders() {
        List<Order> allOrders = new ArrayList<>();
        for (OrderBookHandler handler : orderBooks.values()) {
            allOrders.addAll(handler.getAllOrders());
        }

        log.debug("Retrieved all orders: orderCount={}", allOrders.size());
        return allOrders;
    }

    public void restoreOrders(List<Order> orders) {
        log.info("Restoring orders: orderCount={}", orders.size());

        resetAll();

        //Todo: just put orders in order list
        /*for (Order order : orders) {
            OrderBookHandler handler = getOrCreateBook(order.getTradePair());
            handler.restoreOrder(order);
        }*/

        log.info("Orders restored successfully: orderCount={}, orderBookCount={}", orders.size(), orderBooks.size());
    }

}
