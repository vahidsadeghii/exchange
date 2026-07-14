package com.exchange.me.domain;

public enum MarketType {
    SPOT("SPOT"),
    FUTURES("FUTURES");

    private final String enumValue;

    MarketType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}


