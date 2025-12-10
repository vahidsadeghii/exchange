package com.exchange.me.domain;

public enum TradeSide {
    BUY("BUY"),
    SELL("SELL");

    private String enumValue;
    TradeSide(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
