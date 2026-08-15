package com.exchange.me.handler;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.matching.MatchingContext;
import com.exchange.me.matching.MatchingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
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
