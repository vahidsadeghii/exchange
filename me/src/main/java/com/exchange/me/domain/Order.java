package com.exchange.me.domain;

public class Order {
    public enum OrderSide {
        BUY,
        SELL
    }

    public enum OrderType {
        LIMIT,
        MARKET
    }

    public enum TradePair {
        BTC_USD,
        ETH_USD,
        LTC_USD
    }

    private final long id;
    private final long timestamp;
    private final long userId;
    private final OrderSide orderSide;
    private final OrderType orderType;
    private final TradePair tradePair;
    private final double quantity;
    private final double price;
    private double filled;

    public Order(long id, long timestamp, long userId, OrderSide orderSide, OrderType orderType,
            TradePair tradePair, double quantity,
            double price) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.orderSide = orderSide;
        this.orderType = orderType;
        this.tradePair = tradePair;
        this.quantity = quantity;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getUserId() {
        return userId;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public TradePair getTradePair() {
        return tradePair;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getRemainingQuantity() {
        return quantity - filled;
    }

    public double getFilled() {
        return filled;
    }

    public void setFilled(double filled) {
        this.filled = filled;
    }
}
