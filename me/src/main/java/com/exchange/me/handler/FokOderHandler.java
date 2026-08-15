package com.exchange.me.handler;


import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradeSide;
import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.exception.FokOrderPriceCanNotBeNullException;
import com.exchange.me.matching.MatchingContext;
import com.exchange.me.matching.MatchingEngine;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FokOderHandler implements OrderHandler {
    private final MatchingEngine matchingEngine;

    private static final double ZERO_PRICE = 0.0;

    public FokOderHandler(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

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

            return List.of();
        }

        // Execute matchingfff
        if (order.getTradeSide() == TradeSide.BUY) {
            return matchingEngine.executeBuyOrder(timestamp, order, context.getAsks(), context.getOrderIndex());
        } else {
            return matchingEngine.executeSellOrder(timestamp, order, context.getBids(), context.getOrderIndex());
        }
    }

    private void validateFokOrder(Order order) {
        if (order.getOrderType() == OrderType.FOK && order.getPrice() <= ZERO_PRICE) {
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
