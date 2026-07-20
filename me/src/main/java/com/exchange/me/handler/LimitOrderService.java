package com.exchange.me.handler;


import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.TradeSide;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimitOrderService {

    private final OrderMatchingUtility orderMatchingUtility;


    public List<MatchInfo> execute(
            long timestamp,
            Order limitOrder,
            TreeMap<Long, Deque<Order>> bids,
            TreeMap<Long, Deque<Order>> asks,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex,
            AddToBookCallback addToBook) {

        List<MatchInfo> matches;

        if (limitOrder.getTradeSide() == TradeSide.BUY) {
            matches = orderMatchingUtility.executeBuyOrder(timestamp, limitOrder, asks, orderIndex);
        } else {
            matches = orderMatchingUtility.executeSellOrder(timestamp, limitOrder, bids, orderIndex);
        }

        // Add remaining quantity to order book
        if (limitOrder.getRemainingQuantity() > 0) {
            addToBook.addOrder(limitOrder);
            log.debug("LIMIT order {} partially filled, remainder added to book", limitOrder.getId());
        }

        return matches;
    }

    @FunctionalInterface
    public interface AddToBookCallback {
        void addOrder(Order order);
    }

}
