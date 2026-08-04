package com.exchange.me.matching;

import com.exchange.me.domain.MatchInfo;
import com.exchange.me.domain.Order;
import com.exchange.me.domain.OrderType;
import com.exchange.me.domain.TradeSide;
import com.exchange.me.handler.OrderBookHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Core matching component responsible for executing order matching logic
 * against the order book.
 *
 * <p>
 * The engine processes incoming {@link Order} objects and uses
 * {@link MatchingContext} to access the current order book state,
 * including bids, asks, and active order indexes.
 *
 * <p>
 * Each successful execution produces {@link MatchInfo} records
 * representing completed trades.
 *
 * @see MatchingContext
 * @see Order
 * @see MatchInfo
 */
@Component
@Slf4j
public class MatchingEngine {

    //Generic matching logic Limit and Market
    public List<MatchInfo> executeBuyOrder(
            long timestamp,
            Order buyOrder,
            TreeMap<Long, Deque<Order>> asks,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        List<MatchInfo> matches = new ArrayList<>();

        while (!asks.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
            Map.Entry<Long, Deque<Order>> bestAsk = asks.firstEntry();
            if (bestAsk == null) {
                break;
            }

            long askPrice = bestAsk.getKey();

            // Price check - LIMIT orders
            if (buyOrder.getOrderType() == OrderType.LIMIT &&
                    askPrice > buyOrder.getPrice()) {
                break;
            }

            Deque<Order> askList = bestAsk.getValue();
            matchOrdersAtLevel(timestamp, buyOrder, askList, askPrice, matches, orderIndex);

            if (askList.isEmpty()) {
                asks.pollFirstEntry();
            }
        }

        return matches;
    }

    // Generic matching logic - for Limit and Market
    public List<MatchInfo> executeSellOrder(
            long timestamp,
            Order sellOrder,
            TreeMap<Long, Deque<Order>> bids,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        List<MatchInfo> matches = new ArrayList<>();

        while (!bids.isEmpty() && sellOrder.getRemainingQuantity() > 0) {
            Map.Entry<Long, Deque<Order>> bestBid = bids.firstEntry();
            if (bestBid == null) {
                break;
            }

            long bidPrice = bestBid.getKey();

            if (sellOrder.getOrderType() == OrderType.LIMIT &&
                    bidPrice < sellOrder.getPrice()) {
                break;
            }

            Deque<Order> bidList = bestBid.getValue();
            matchOrdersAtLevel(timestamp, sellOrder, bidList, bidPrice, matches, orderIndex);

            if (bidList.isEmpty()) {
                bids.pollFirstEntry();
            }
        }

        return matches;
    }

    /**
     * Matches orders at a specific price level
     */
    public void matchOrdersAtLevel(
            long timestamp,
            Order incomingOrder,
            Deque<Order> levelOrders,
            long priceLevel,
            List<MatchInfo> matches,
            Map<Long, OrderBookHandler.OrderLocation> orderIndex) {

        while (!levelOrders.isEmpty() && incomingOrder.getRemainingQuantity() > 0) {
            Order levelOrder = levelOrders.peek();
            if (levelOrder == null) {
                break;
            }

            double tradedQuantity = Math.min(
                    incomingOrder.getRemainingQuantity(),
                    levelOrder.getRemainingQuantity()
            );

            incomingOrder.setFilled(incomingOrder.getFilled() + tradedQuantity);
            levelOrder.setFilled(levelOrder.getFilled() + tradedQuantity);

            createMatch(timestamp, incomingOrder, levelOrder, tradedQuantity, priceLevel, matches);

            if (levelOrder.getRemainingQuantity() == 0) {
                levelOrders.poll();
                orderIndex.remove(levelOrder.getId());
            }
        }
    }


    // Creates a match record
    public void createMatch(
            long timestamp,
            Order incomingOrder,
            Order levelOrder,
            double tradedQuantity,
            long priceLevel,
            List<MatchInfo> matches) {

        TradeSide incomingSide = incomingOrder.getTradeSide();
        matches.add(
                new MatchInfo(
                        UUID.randomUUID().toString(),
                        timestamp,
                        System.currentTimeMillis(),
                        incomingSide,
                        incomingOrder.getId(),
                        levelOrder.getId(),
                        incomingOrder.getUserId(),
                        levelOrder.getUserId(),
                        tradedQuantity,
                        priceLevel,
                        incomingOrder.getQuantity(),
                        incomingOrder.getRemainingQuantity(),
                        levelOrder.getQuantity(),
                        levelOrder.getRemainingQuantity()
                ));

        log.info(
                "MATCH: taker={}, maker={}, qty={}, price={}",
                incomingOrder.getId(),
                levelOrder.getId(),
                tradedQuantity,
                priceLevel
        );

        log.debug("Match created: {} order {} ({}) matched with {} order {} ({}) at price {} qty {}",
                incomingSide, incomingOrder.getId(), incomingOrder.getUserId(),
                levelOrder.getTradeSide(), levelOrder.getId(), levelOrder.getUserId(),
                priceLevel, tradedQuantity);
    }

}

