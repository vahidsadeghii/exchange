package com.exchange.service;

public interface  EngineService {
    public void processOrder(long orderId, long userId, String symbol, boolean isBuyOrder, double quantity, double price);
}
