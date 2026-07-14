package com.exchange.me.domain;


public enum OrderType {
    LIMIT("LIMIT"),
    MARKET("MARKET"),
    FOK("FOK");

    private final String enumValue;

    OrderType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
