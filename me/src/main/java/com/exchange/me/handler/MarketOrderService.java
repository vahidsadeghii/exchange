package com.exchange.me.handler;


import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.TradeSide;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class MarketOrderService {

    private final OrderMatchingUtility orderMatchingUtility;

       public List<MatchInfo> execute(
            long timestamp,
            Order marketOrder,
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        if (marketOrder.getTradeSide() == TradeSide.BUY) {
            return orderMatchingUtility.executeBuyOrder(timestamp, marketOrder, asks, orderIndex);
        } else {
            return orderMatchingUtility.executeSellOrder(timestamp, marketOrder, bids, orderIndex);
        }
    }
}
