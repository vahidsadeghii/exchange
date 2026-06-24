package com.exchange.oms.service;

import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import com.exchange.oms.domain.*;

import java.math.BigDecimal;

public interface OrderService {

     Order createOrder(Long onlineUser, AssetType assetType, TradePair tradePair, TradeSide tradeSide,
                       OrderType orderType, BigDecimal quantity, BigDecimal price);

     Order updateOrder(long orderId, long userId, MatchEventStatus orderStatus);

     OrderBookResponse getOrder(long orderId);
}
