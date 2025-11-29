package com.exchange.me.domain;

public class Order {
    public enum OrderSide {
        BUY,
        SELL
    }

    private final long id;
    private final long timestamp;
    private final long userId;
    private final String symbol;
    private final OrderSide orderSide;
    private final double quantity;
    private final double price;
    private double filled;

    public Order(long id, long timestamp, long userId, String symbol, OrderSide orderSide, double quantity,
            double price) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.symbol = symbol;
        this.orderSide = orderSide;
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

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getOrderSide() {
        return orderSide;
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
