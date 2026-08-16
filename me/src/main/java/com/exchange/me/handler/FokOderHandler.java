package com.exchange.me.handler;


import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.exception.FokOrderPriceCanNotBeNullException;
import com.exchange.me.matching.MatchingContext;
import com.exchange.me.matching.MatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class FokOderHandler implements OrderHandler {
    private final MatchingEngine matchingEngine;

    private static final double ZERO_PRICE = 0.0;

    @Override
    public OrderType supports() {
        return OrderType.FOK;
    }

    @Override
    public List<MatchInfo> execute(long timestamp,
                                   Order order,
                                   MatchingContext context) {

        validateFokOrder(order);

        // Check if order can be fully filled
        if (!canFullyFill(order, context.getBids(), context.getAsks())) {
            log.debug("FOK order {} cannot be fully filled, killing order. Required: {}, Available: {}",
                    order.getId(),
                    order.getQuantity(),
                    calculateAvailableQuantity(order, context.getBids(), context.getAsks()));
            return List.of();
        }

        // Execute matchingfff
        log.debug("FOK order {} can be fully filled, executing", order.getId());
        if (order.getTradeSide() == TradeSide.BUY) {
            return matchingEngine.executeBuyOrder(timestamp, order, context.getAsks(), context.getOrderIndex());
        } else {
            return matchingEngine.executeSellOrder(timestamp, order, context.getBids(), context.getOrderIndex());
        }
    }

    private void validateFokOrder(Order order) {
        if (order.getOrderType() == OrderType.FOK && order.getPrice() <= ZERO_PRICE) {
            log.error("FOK order {} has invalid price: {}", order.getId(), order.getPrice());
            throw new FokOrderPriceCanNotBeNullException();
        }
    }

    private boolean canFullyFill(
            Order order,
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks) {

        double availableQuantity = calculateAvailableQuantity(order, bids, asks);
        return availableQuantity >= order.getRemainingQuantity();
    }

    //Calculates available quantity in the order book for this order.
    private double calculateAvailableQuantity(
            Order order,
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks) {

        double availableQuantity = 0;

        if (order.getTradeSide() == TradeSide.BUY) {
            for (Map.Entry<Long, Deque<Order>> entry : asks.entrySet()) {
                long askPrice = entry.getKey();
                if (askPrice > order.getPrice()) {
                    break;
                }

                for (Order ask : entry.getValue()) {
                    availableQuantity += ask.getRemainingQuantity();
                }
            }
        } else {
            for (Map.Entry<Long, Deque<Order>> entry : bids.entrySet()) {
                long bidPrice = entry.getKey();
                if (bidPrice < order.getPrice()) {
                    break;
                }

                for (Order bid : entry.getValue()) {
                    availableQuantity += bid.getRemainingQuantity();
                }
            }
        }

        return availableQuantity;
    }
}
