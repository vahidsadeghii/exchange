package com.exchange.oms.domain;

public enum OrderType {
    BUY("BUY"),
    SELL("SELL");

    private String enumValue;
    OrderType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
