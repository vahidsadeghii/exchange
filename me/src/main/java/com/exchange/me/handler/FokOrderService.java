package com.exchange.me.handler;


import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.exception.FokOrderPriceCanNotBeNullException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FokOrderService {

    private static final double ZERO_PRICE = 0.0;
    private final OrderMatchingUtility orderMatchingUtility;


    /**
     * Executes a FOK (Fill Or Kill) order.
     * Order is filled only if entire quantity can be matched immediately.
     * If full execution is not possible, order is cancelled (killed).
     */
    public List<MatchInfo> execute(
            long timestamp,
            Order fokOrder,
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        validateFokOrder(fokOrder);

        // Check if order can be fully filled
        if (!canFullyFill(fokOrder, bids, asks)) {
            log.debug("FOK order {} cannot be fully filled, killing order. Required: {}, Available: {}",
                    fokOrder.getId(),
                    fokOrder.getQuantity(),
                    calculateAvailableQuantity(fokOrder, bids, asks));
            return List.of();
        }

        // Execute matching
        log.debug("FOK order {} can be fully filled, executing", fokOrder.getId());
        if (fokOrder.getTradeSide() == TradeSide.BUY) {
            return orderMatchingUtility.executeBuyOrder(timestamp, fokOrder, asks, orderIndex);
        } else {
            return orderMatchingUtility.executeSellOrder(timestamp, fokOrder, bids, orderIndex);
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