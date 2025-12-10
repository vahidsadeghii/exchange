package com.exchange.me.domain;

public enum OrderType {
    LIMIT("LINIT"),
    MARKET("MARKET");

    private String enumValue;

    OrderType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
