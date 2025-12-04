package com.exchange.me.domain;

public enum OrderSide {
    BUY("BUY"),
    SELL("SELL");

    private String enumValue;
    OrderSide(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
