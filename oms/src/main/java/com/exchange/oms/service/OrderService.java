package com.exchange.oms.service;

import com.exchange.oms.controller.order.findorderbook.OrderBookResponse;
import com.exchange.oms.domain.*;

public interface OrderService {

     Order createOrder(long userId, TradePair tradePair, OrderType orderType, double quantity, double price);

     Order updateOrder(long orderId, long userId, MatchEngineStatus orderStatus);

     OrderBookResponse getOrder(long orderId);
}
