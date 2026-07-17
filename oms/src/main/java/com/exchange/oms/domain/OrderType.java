package com.exchange.oms.domain;

public enum OrderType {
    LIMIT("LINIT"),
    MARKET("MARKET"),
    FOK("FOK");

    private String enumValue;

    OrderType(String enumValue) {
        this.enumValue = enumValue;
    }

    @Override
    public String toString() {
        return enumValue;
    }
}
