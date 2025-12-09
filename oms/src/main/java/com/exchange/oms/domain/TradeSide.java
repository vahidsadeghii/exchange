package com.exchange.oms.domain;

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
