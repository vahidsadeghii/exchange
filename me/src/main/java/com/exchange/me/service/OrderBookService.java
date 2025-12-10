package com.exchange.me.service;

import com.exchange.me.domain.*;
import com.exchange.me.handler.OrderBookHandler;

import java.util.List;

public interface OrderBookService {

   List<MatchInfo> createNewOrder(long timestamp, long orderId, long userId,
                                  TradeSide orderSide,
                                  TradePair tradePair,
                                  OrderType orderType,
                                  double quantity,
                                  double price);
    void deleteOrder(long timestamp, Order order);

    Order getOrder(TradePair pair, long orderId);

    OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels);

    void resetAll();
}
