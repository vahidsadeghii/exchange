package com.exchange.me.service.impl;

import com.exchange.me.domain.*;
import com.exchange.me.exception.InvalidTradPairException;
import com.exchange.me.exception.InvalidTradePairException;
import com.exchange.me.exception.NotFoundOrderBookHandlerException;
import com.exchange.me.exception.OrderCanNotBeNullException;
import com.exchange.me.handler.OrderBookHandler;
import com.exchange.me.handler.OrderHandlerFactory;
import com.exchange.me.service.EngineService;
import com.exchange.me.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class EngineServiceImpl implements EngineService {
    private final MatchEventService matchEventService;
    private final OrderHandlerFactory orderHandlerFactory;


    private final Map<TradePair, OrderBookHandler> orderBooks = new ConcurrentHashMap<>();

    @Override
    public MatchEngine createUpdateOrder(Long oldOrderId, long orderId, long userId, TradeSide tradeSide,
                                         TradePair tradePair, OrderType orderType, MarketType marketType,
                                         double quantity, double price) {
        if (tradePair == null) {
            throw new InvalidTradPairException();
        }

        OrderBookHandler handler = getOrCreateBook(tradePair);

        //Cancel old order
        if (oldOrderId != null) {
            try {
                Optional<Order> oldOrder = handler.getOrder(oldOrderId);
                oldOrder.ifPresentOrElse(
                        order -> {
                            handler.deleteOrder(System.currentTimeMillis(), order);
                            log.info("Old order cancelled: {}", oldOrderId);
                        },
                        () -> log.warn("Old order not found for cancellation: {}", oldOrderId)
                );
            } catch (Exception e) {
                log.warn("Failed to delete old order {}: {}", oldOrderId, e.getMessage(), e);
                // Continue with new order creation despite cancellation failure
            }
        }

        //create new order
        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .tradeSide(tradeSide)
                .orderType(orderType)
                .tradePair(tradePair)
                .marketType(marketType)
                .quantity(quantity)
                .price(price)
                .build();

        long timestamp = System.currentTimeMillis();
        List<MatchInfo> matches = handler.matchOrder(timestamp, order);

        if (order.getRemainingQuantity() == 0) {
            order.setMatchEngineStatus(MatchEventStatus.FILLED);
        } else if (order.getOrderType() == OrderType.FOK && matches.isEmpty()) {
            order.setMatchEngineStatus(MatchEventStatus.REJECTED);
        } else {
            order.setMatchEngineStatus(MatchEventStatus.PARTIALLY_FILLED);
        }

        log.debug("Order matched and persisting: {}", orderId);

        return matchEventService.saveMatchEvent(order);
    }

    @Override
    public void deleteOrder(long timestamp, Order order) {
        if (order == null) {
            throw new OrderCanNotBeNullException();
        }

        TradePair tradePair = order.getTradePair();
        if (tradePair == null) {
            throw new InvalidTradePairException();
        }

        OrderBookHandler handler = orderBooks.get(order.getTradePair());
        if (handler != null) {
            handler.deleteOrder(timestamp, order);
            log.debug("Order deleted: id={}, pair={}", order.getId(), tradePair);
        } else {
            log.warn("OrderBookHandler not found for pair: {}, order: {}", tradePair, order.getId());
        }
    }

    @Override
    public Order getOrder(TradePair pair, long orderId) {
        if (pair == null) {
            throw new InvalidTradPairException();
        }

        OrderBookHandler handler = orderBooks.get(pair);
        if (handler == null) {
            throw new NotFoundOrderBookHandlerException();
        }

        return handler.getOrder(orderId).orElseThrow(OrderCanNotBeNullException::new);
    }

    @Override
    public OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels) {
        if (levels <= 0) {
            throw new IllegalArgumentException("Market depth levels must be positive: " + levels);
        }

        OrderBookHandler handler = orderBooks.get(pair);
        if (handler == null) {
            log.debug("No market depth available - order book not found for pair: {}", pair);
            return null;
        }

        return handler.getMarketDepth(levels);
    }

    @Override
    public void resetAll() {
        orderBooks.values().forEach(OrderBookHandler::reset);
        log.info("All order books reset - {} pairs cleared", orderBooks.size());
    }

    @Override
    public OrderBookHandler getOrderBook(TradePair pair) {
        return orderBooks.get(pair);
    }

    @Override
    public OrderBookDepth getOrderBookDepth(TradePair pair, int depth) {
        if (pair == null) {
            throw new InvalidTradPairException();
        }

        if (depth <= 0) {
            throw new IllegalArgumentException("Depth must be positive: " + depth);
        }

        OrderBookHandler book = getOrderBook(pair);

        if (book == null) {
            throw new NotFoundOrderBookHandlerException();
        }

        List<PriceLevel> bids = book.getBidsList(depth)
                .stream()
                .map(level -> new PriceLevel(
                        level.price(),
                        level.volume(),
                        level.orderCount()))
                .toList();

        List<PriceLevel> asks = book.getAsksList(depth)
                .stream()
                .map(level -> new PriceLevel(
                        level.price(),
                        level.volume(),
                        level.orderCount()))
                .toList();

        return new OrderBookDepth(bids, asks);
    }

    private OrderBookHandler getOrCreateBook(TradePair pair) {

        return orderBooks.computeIfAbsent(pair, p -> {

            log.info("Creating OrderBookHandler for {}", p);

            return new OrderBookHandler(
                    p,
                    orderHandlerFactory);
        });
    }
}