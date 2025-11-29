package com.exchange.oms.service;

import com.exchange.oms.domain.Order;
import com.exchange.oms.domain.OrderType;
import com.exchange.oms.domain.TradePair;

public interface OrderService {
    public Order createOrder(TradePair tradePair, OrderType orderType, double quantity, double price);
}
