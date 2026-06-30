package com.exchange.me.service;


import com.exchange.me.domain.*;
import com.exchange.me.handler.OrderBookHandler;

public interface EngineService {

    MatchEngine createUpdateOrder(Long oldOrderId, long orderId, long userId,
                                  TradeSide orderSide,
                                  TradePair tradePair,
                                  OrderType orderType,
                                  MarketType marketType,
                                  double quantity,
                                  double price);

    void deleteOrder(long timestamp, Order order);

    Order getOrder(TradePair pair, long orderId);

    OrderBookHandler.MarketDepth getMarketDepth(TradePair pair, int levels);

    void resetAll();
}
