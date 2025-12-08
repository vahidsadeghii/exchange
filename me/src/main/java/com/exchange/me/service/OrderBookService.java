package com.exchange.me.service;

import com.exchange.me.domain.*;

import java.util.List;

public interface OrderBookService {

   List<MatchInfo> createNewOrder(long timestamp, long orderId, long userId,
                                  TradeSide orderSide,
                                  TradePair tradePair,
                                  OrderType orderType,
                                  double quantity,
                                  double price

    );
}
