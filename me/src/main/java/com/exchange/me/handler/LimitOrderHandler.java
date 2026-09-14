package com.exchange.me.handler;

import com.exchange.me.sbe.OrderType;
import com.exchange.me.sbe.TradeSide;
import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.matching.MatchingContext;
import com.exchange.me.matching.MatchingEngine;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LimitOrderHandler implements OrderHandler {
    private final MatchingEngine matchingEngine;

    @Override
    public OrderType supports() {
        return OrderType.LIMIT;
    }

    @Override
    public List<MatchInfo> execute(
            long timestamp,
            Order order,
            MatchingContext context) {

        List<MatchInfo> matches;

        if (order.getTradeSide() == TradeSide.BUY) {

            matches = matchingEngine.executeBuyOrder(
                    timestamp,
                    order,
                    context.getAsks(),
                    context.getOrderIndex());

        } else {

            matches = matchingEngine.executeSellOrder(
                    timestamp,
                    order,
                    context.getBids(),
                    context.getOrderIndex());
        }

        if (order.getRemainingQuantity() > 0) {
            context.addOrder(order);
        }

        return matches;
    }
}
