package com.exchange.me_core.handler;

import com.exchange.core.sbe.OrderType;
import com.exchange.core.sbe.TradeSide;
import com.exchange.me_core.domain.MatchInfo;
import com.exchange.me_core.domain.Order;
import com.exchange.me_core.matching.MatchingContext;
import com.exchange.me_core.matching.MatchingEngine;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class MarketOrderHandler implements OrderHandler {
    private final MatchingEngine matchingEngine;

    @Override
    public OrderType supports() {
        return OrderType.MARKET;
    }

    @Override
    public List<MatchInfo> execute(
            long timestamp,
            Order order,
            MatchingContext context) {

        if (order.getTradeSide() == TradeSide.BUY) {

            return matchingEngine.executeBuyOrder(
                    timestamp,
                    order,
                    context.getAsks(),
                    context.getOrderIndex());
        }

        return matchingEngine.executeSellOrder(
                timestamp,
                order,
                context.getBids(),
                context.getOrderIndex());
    }
}
